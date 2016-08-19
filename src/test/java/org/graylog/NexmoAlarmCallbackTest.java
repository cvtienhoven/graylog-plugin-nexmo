package org.graylog;

import org.graylog.NexmoAlarmCallback;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.MessageSummary;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.AlertCondition.CheckResult;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.streams.Stream;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableMap;
import com.nexmo.messaging.sdk.NexmoSmsClient;
import com.nexmo.messaging.sdk.messages.TextMessage;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class NexmoAlarmCallbackTest {
	

	private static final ImmutableMap<String, Object> VALID_CONFIG = ImmutableMap.<String, Object> builder()
			.put("base_url", "https://test_base_url")
			.put("api_key", "test_api_key")
			.put("api_secret", "test_api_secret")
			.put("connection_timeout", 2)
			.put("read_timeout", 3)
			.put("from", "test_from")
			.put("to", "3654323423423")
			.put("text", "Testing").build();
	
	private static final ImmutableMap<String, Object> INVALID_CONFIG = ImmutableMap.<String, Object> builder()
			.put("base_url", "")
			.put("api_key", "test_api_key")
			.put("api_secret", "test_api_secret")
			.put("connection_timeout", 2)
			.put("read_timeout", 3)
			.put("from", "test_from")
			.put("to", "354323423423")
			.put("text", "Testing").build();
	
	private static final ImmutableMap<String, Object> VALID_CONFIG_PLACEHOLDERS = ImmutableMap.<String, Object> builder()
			.put("base_url", "https://test_base_url")
			.put("api_key", "test_api_key")
			.put("api_secret", "test_api_secret")
			.put("connection_timeout", 2)
			.put("read_timeout", 3)
			.put("from", "test_from")
			.put("to", "32523423423")
			.put("text", "Testing [stream] and [source]").build();
	
	private static final ImmutableMap<String, Object> VALID_CONFIG_MULTIPLE_NUMBERS = ImmutableMap.<String, Object> builder()
			.put("base_url", "https://test_base_url")
			.put("api_key", "test_api_key")
			.put("api_secret", "test_api_secret")
			.put("connection_timeout", 2)
			.put("read_timeout", 3)
			.put("from", "test_from")
			.put("to", "3654323423423,36542343,365432433")
			.put("text", "Testing").build();
	
	private static final Configuration VALID_CONFIGURATION = new Configuration(VALID_CONFIG);
	private static final Configuration VALID_CONFIGURATION_PLACEHOLDERS= new Configuration(VALID_CONFIG_PLACEHOLDERS);
	private static final Configuration VALID_CONFIGURATION_MULTIPLE_NUMBERS= new Configuration(VALID_CONFIG_MULTIPLE_NUMBERS);

	private NexmoAlarmCallback alarmCallback;

	@Before
	public void setUp() {
		alarmCallback = new NexmoAlarmCallback();
	}

	@Test
	public void testInitialize() throws AlarmCallbackConfigurationException {
		final Configuration configuration = new Configuration(VALID_CONFIG);
		alarmCallback.initialize(configuration);
	}

	@Test(expected = ConfigurationException.class)
	public void testConfigurationSucceedsWithInvalidConfiguration()
			throws AlarmCallbackConfigurationException, ConfigurationException {
		alarmCallback.initialize(new Configuration(INVALID_CONFIG));
		alarmCallback.checkConfiguration();
	}

	@Test
	public void testConfigurationSucceedsWithValidConfiguration()
			throws AlarmCallbackConfigurationException, ConfigurationException {
		alarmCallback.initialize(new Configuration(VALID_CONFIG));
		alarmCallback.checkConfiguration();
	}

	@Test
	public void testGetName() {
		assertEquals("Nexmo Alarm Callback", alarmCallback.getName());
	}
	
	
	@Test
	public void testCall() throws Exception {
		DateTime dateTime = new DateTime(2015, 11, 18, 12, 7, DateTimeZone.UTC);
		
		final NexmoSmsClient client = mock(NexmoSmsClient.class);
		final Stream stream = mockStream();
		final AlertCondition.CheckResult checkResult = mockCheckResult(dateTime);//mock(AlertCondition.CheckResult.class);
		final AlertCondition alertcondition = mockAlertCondition();

		when(checkResult.getTriggeredCondition()).thenReturn(alertcondition);

		alarmCallback.initialize(VALID_CONFIGURATION);
		alarmCallback.setClient(client);
		alarmCallback.checkConfiguration();
		alarmCallback.call(stream, checkResult);

		verify(client).submitMessage(Mockito.any(TextMessage.class));
	}
	
	@Test
	public void testCallWithPlaceholders() throws Exception {
		DateTime dateTime = new DateTime(2015, 11, 17, 12, 9, DateTimeZone.UTC);
		
		final NexmoSmsClient client = mock(NexmoSmsClient.class);
		final Stream stream = mockStream();
		final AlertCondition.CheckResult checkResult = mockCheckResult(dateTime);
		final AlertCondition alertcondition = mockAlertCondition();

		when(checkResult.getTriggeredCondition()).thenReturn(alertcondition);

		alarmCallback.initialize(VALID_CONFIGURATION_PLACEHOLDERS);
		alarmCallback.setClient(client);
		alarmCallback.checkConfiguration();
		alarmCallback.call(stream, checkResult);
	
		
		ArgumentCaptor<TextMessage> argument = ArgumentCaptor.forClass(TextMessage.class);
		
		verify(client).submitMessage(argument.capture());
		assertEquals("test_from", argument.getValue().getFrom());
		assertEquals("32523423423", argument.getValue().getTo());
		assertEquals("Testing Stream title and test_source1", argument.getValue().getMessageBody());
	}
	
	@Test
	public void testCallWithMultipleNumbers() throws Exception {
		DateTime dateTime = new DateTime(2015, 11, 17, 12, 9, DateTimeZone.UTC);
		
		final NexmoSmsClient client = mock(NexmoSmsClient.class);
		final Stream stream = mockStream();
		final AlertCondition.CheckResult checkResult = mockCheckResult(dateTime);
		final AlertCondition alertcondition = mockAlertCondition();

		when(checkResult.getTriggeredCondition()).thenReturn(alertcondition);

		alarmCallback.initialize(VALID_CONFIGURATION_MULTIPLE_NUMBERS);
		alarmCallback.setClient(client);
		alarmCallback.checkConfiguration();
		alarmCallback.call(stream, checkResult);
	
		
		ArgumentCaptor<TextMessage> argument = ArgumentCaptor.forClass(TextMessage.class);
		
		verify(client, Mockito.times(3)).submitMessage(argument.capture());
	
		assertEquals("3654323423423", argument.getAllValues().get(0).getTo());
		assertEquals("36542343", argument.getAllValues().get(1).getTo());
		assertEquals("365432433", argument.getAllValues().get(2).getTo());
	}
	
	private AlertCondition mockAlertCondition() {
		final String alertConditionId = "alertConditionId";
		final AlertCondition alertCondition = mock(AlertCondition.class);
		when(alertCondition.getId()).thenReturn(alertConditionId);
		when(alertCondition.getDescription()).thenReturn("alert description");		
		return alertCondition;
	}

	private Stream mockStream() {
		// final String alertConditionId = "alertConditionId";
		final Stream stream = mock(Stream.class);
		when(stream.getTitle()).thenReturn("Stream title");
		return stream;
	}
	
	private CheckResult mockCheckResult(DateTime dateTime){
		final CheckResult result = mock(CheckResult.class);
		List<MessageSummary> messages = new ArrayList<MessageSummary>();
		
		Message message1 = mock(Message.class);
		when(message1.getId()).thenReturn("test_id1");
		when(message1.getSource()).thenReturn("test_source1");
		when(message1.getMessage()).thenReturn("test_message1");
		
		Message message2 = mock(Message.class);
		when(message2.getId()).thenReturn("test_id2");
		when(message2.getSource()).thenReturn("test_source2");
		when(message2.getMessage()).thenReturn("test_message2");

		
		MessageSummary messageSummary1 = new MessageSummary("index1", message1);
		messages.add(messageSummary1);
		
		MessageSummary messageSummary2 = new MessageSummary("index2", message2);
		messages.add(messageSummary2);
		
		when(result.getMatchingMessages()).thenReturn(messages);
		when(result.getTriggeredAt()).thenReturn(dateTime);
		when(result.getResultDescription()).thenReturn("Result description");
		return result;
	}
}
