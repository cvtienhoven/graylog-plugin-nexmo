package org.graylog;


import java.net.URI;
import java.util.Map;



import org.graylog2.plugin.alarms.AlertCondition.CheckResult;
import org.graylog2.plugin.alarms.callbacks.AlarmCallback;

import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.configuration.ConfigurationRequest;

import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.configuration.fields.NumberField;
import org.graylog2.plugin.streams.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.nexmo.messaging.sdk.NexmoSmsClient;
import com.nexmo.messaging.sdk.messages.TextMessage;



public class NexmoAlarmCallback implements AlarmCallback {
	private static final String BASE_URL = "base_url";
	private static final String API_KEY = "api_key";
	private static final String API_SECRET = "api_secret";
	private static final String CONNECTION_TIMEOUT = "connection_timeout";
	private static final String READ_TIMEOUT = "read_timeout";
	private static final String FROM = "from";
	private static final String TO = "to";
	private static final String TEXT = "text";
	private static final Logger LOG = LoggerFactory.getLogger(NexmoAlarmCallback.class);
	
	private Configuration configuration;
	private NexmoSmsClient client;
	
	@Override
	public void call(Stream stream, CheckResult result) throws AlarmCallbackException {
		String text = configuration.getString(TEXT);
		if (result.getMatchingMessages() != null && result.getMatchingMessages().size() > 0){
			text = text.replace("[source]", result.getMatchingMessages().get(0).getSource());
		}
		text = text.replace("[stream]", stream.getTitle());
				

		try {
			for (String to : configuration.getString(TO).split(",")){
				TextMessage message = new TextMessage(configuration.getString(FROM), to.trim(), text);
				client.submitMessage(message);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.error(e.getStackTrace().toString());
			throw new AlarmCallbackException(e.getMessage());
		}

	}

	

	@Override
	public void checkConfiguration() throws ConfigurationException {		
		
		if (!configuration.stringIsSet(BASE_URL)) {
			throw new ConfigurationException(BASE_URL + " is mandatory and must be not be null or empty.");
		}
		if (!configuration.getString(BASE_URL).startsWith("https://")) {
			throw new ConfigurationException(BASE_URL + " should start with https://.");
		}
		if (!configuration.stringIsSet(API_KEY)) {
			throw new ConfigurationException(API_KEY + " is mandatory and must be not be null or empty.");
		}
		if (!configuration.stringIsSet(API_SECRET)) {
			throw new ConfigurationException(API_SECRET + " is mandatory and must be not be null or empty.");
		}
		if (!configuration.intIsSet(CONNECTION_TIMEOUT)) {
			throw new ConfigurationException(CONNECTION_TIMEOUT + " is mandatory and must be not be null or empty.");
		}
		if (!configuration.intIsSet(READ_TIMEOUT)) {
			throw new ConfigurationException(READ_TIMEOUT + " is mandatory and must be not be null or empty.");
		}
		if (!configuration.stringIsSet(FROM)) {
			throw new ConfigurationException(FROM + " is mandatory and must be not be null or empty.");
		}
		if (!configuration.stringIsSet(TO)) {
			throw new ConfigurationException(TO + " is mandatory and must be not be null or empty.");
		}
		if (configuration.getString(TO).contains(",")) {
			String[] to = configuration.getString(TO).split(",");
			for (String number : to) {
				if (number.trim().equals("")) {
					throw new ConfigurationException("Cannot submit empty phone number.");
				}
			}
		}
		
		if (!configuration.stringIsSet(TEXT)) {
			throw new ConfigurationException(TEXT + " is mandatory and must be not be null or empty.");
		}

	}

	@Override
	public Map<String, Object> getAttributes() {
		return Maps.transformEntries(configuration.getSource(), new Maps.EntryTransformer<String, Object, Object>() {
			@Override
			public Object transformEntry(String key, Object value) {
				if (API_SECRET.equals(key)) {
					return "****";
				}
				return value;
			}
		});
	}
	
	@VisibleForTesting
	void setClient(NexmoSmsClient client) {
		this.client = client;
	}
	
	@Override
	public String getName() {
		return "Nexmo Alarm Callback";
	}

	@Override
	public ConfigurationRequest getRequestedConfiguration() {
		final ConfigurationRequest configurationRequest = new ConfigurationRequest();
		
		configurationRequest.addField(new TextField(BASE_URL, "Base URL", "https://rest.nexmo.com",
				"The base url of the Nexmo API.", ConfigurationField.Optional.NOT_OPTIONAL));
		configurationRequest.addField(new TextField(API_KEY, "API Key", "",
				"", ConfigurationField.Optional.NOT_OPTIONAL));
		configurationRequest.addField(new TextField(API_SECRET, "API Secret", "",
				"", ConfigurationField.Optional.NOT_OPTIONAL, TextField.Attribute.IS_PASSWORD));		
		configurationRequest.addField(
				new NumberField(CONNECTION_TIMEOUT, "Connection Timeout ", 5000, "The connection timeout in milliseconds.",
						ConfigurationField.Optional.NOT_OPTIONAL));
		configurationRequest.addField(
				new NumberField(READ_TIMEOUT, "Read Timeout ", 30000, "The read timeout in milliseconds.",
						ConfigurationField.Optional.NOT_OPTIONAL));
		configurationRequest.addField(new TextField(FROM, "From", "Graylog",
				"", ConfigurationField.Optional.NOT_OPTIONAL));
		configurationRequest.addField(new TextField(TO, "To", "",
				"The phone number(s) that should be texted. Can be a comma separated list.", ConfigurationField.Optional.NOT_OPTIONAL));
		configurationRequest.addField(new TextField(TEXT, "Text", "",
				"The text to send. Replacements: [source] will be replaced by the source field, [stream] will be replaced by the stream title.", ConfigurationField.Optional.NOT_OPTIONAL));
		
		
		return configurationRequest;
	}

	@Override
	public void initialize(Configuration config) {
		this.configuration = config;
		try {
			setClient(new NexmoSmsClient(
					configuration.getString(BASE_URL),
					configuration.getString(API_KEY),
					configuration.getString(API_SECRET),
					configuration.getInt(CONNECTION_TIMEOUT),
					configuration.getInt(READ_TIMEOUT),
					false,
					null				
				));
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}

	}
	
}
