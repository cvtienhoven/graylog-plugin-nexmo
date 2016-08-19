# SensuAlarmCallback Plugin for Graylog

[![Build Status](https://travis-ci.org/cvtienhoven/graylog-plugin-sensu.svg?branch=master)](https://travis-ci.org/cvtienhoven/graylog-plugin-sensu)


**Required Graylog version:** 2.0 and later


This plugin enables you to call the Nexmo SMS API.


## Installation

[Download the plugin](https://github.com/https://github.com/cvtienhoven/graylog-plugin-sensu.git/releases)
and place the `.jar` file in your Graylog plugin directory. The plugin directory
is the `plugins/` folder relative from your `graylog-server` directory by default
and can be configured in your `graylog.conf` file.

Restart `graylog-server` and you are done.

## Use cases

This plugin is useful when you need to receive alerts by SMS and you have a subscription at Nexmo.

## Usage

### Configure the alarm callback

You can configure an alert condition in Graylog and add the `Nexmo Alarm Callback` as the Callback Type. 
In the popup that occurs you can configure the options to send the SMS. You can enter multiple recipients 
(comma separated). In the text field, the values [stream] and [source] will be replaced by the stream title 
and the source of the first message in the message backlog (if any).


Getting started
---------------

This project is using Maven 3 and requires Java 8 or higher.

* Clone this repository.
* Run `mvn package` to build a JAR file.
* Optional: Run `mvn jdeb:jdeb` and `mvn rpm:rpm` to create a DEB and RPM package respectively.
* Copy generated JAR file in target directory to your Graylog plugin directory.
* Restart the Graylog.

Plugin Release
--------------

We are using the maven release plugin:

```
$ mvn release:prepare
[...]
$ mvn release:perform
```

This sets the version numbers, creates a tag and pushes to GitHub. Travis CI will build the release artifacts and upload to GitHub automatically.
