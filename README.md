# sneakerbot
[![CircleCI](https://circleci.com/gh/cameronb23/sneakerbot.svg?style=svg&circle-token=1dd8cb38be83024deb6003ff882f4144a09cb516)](https://circleci.com/gh/cameronb23/sneakerbot)

## Requirements
- Environment with Java 8 installed
- Valid [chromedriver](https://sites.google.com/a/chromium.org/chromedriver/downloads) on your computer
- Proxies with either no auth or user/password auth if you need them
- A drop day that uses a splash page on Adidas
- Patience


## Setup \*outline\*
- setup selenium chromdriver
- package up jar
- run
- etc

## Running
To run this, you need a few things. First, you'll need a copy of the `config.xml`, and the jar itself. Once you have those,
put them somewhere and execute this command (modify to your use case)

```bash
java -Xms[INIT]M -Xmx[MAX]M -jar [jar] [config_file]
```

You can modify this command to suit your needs according to this list of values:
- **INIT**: This is your starting heap size. Just replace the this with the number of MB you want to allocate.
- **MAX**: This is the _max_ heap size. Do the same as the starting heap size.
- **jar**: The location of your JAR file (including extension).
- **config_file**: The location of your bot configuration.

A configured starting command would look like this:

```bash
java -Xms1024M -Xmx4096M -jar sneakerbot.jar config.xml
```

### Proxies

Proxies are set up in the `proxies.txt` file. Currently, only user/pass and proxies without authentication are supported.
If you need a different kind of authentication to be supported(such as ip),
I suggest making a [pull request](https://github.com/cameronb23/sneakerbot/pull/new/master) and creating your own.

Formats are as follows:

- No authentication: `address:port`
- User/password: `address:port:username:password`

### Configuration

There is a default configuration included with the JARs you can download, or you can create your own.
An example configuration is as follows:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config>
    <mainUrl>http://www.adidas.com/</mainUrl>
    <splashUrl>http://www.adidas.com/yeezy</splashUrl>
    <cartUrl>https://www.adidas.com/on/demandware.store/Sites-adidas-US-Site/en_US/Cart-Show</cartUrl>
    <requestDelay>5000</requestDelay>
    <taskCount>50</taskCount>
    <chromeDriverPath>~/Downloads/chromedriver</chromeDriverPath>
    <useragent>Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3095.0 Safari/537.36</useragent>
    <selectors>
        <selector>[data-sitekey]</selector>
    </selectors>
    <onePass>false</onePass>
    <proxies>~/proxies.txt</proxies>
</config>
```
#### Configuration options:
- **mainUrl**: This is used to set your default Adidas region
- **splashUrl**: This is the _SPLASH_ page address.
- **cartUrl**: This is the link to view your cart once you've carted a product
- **requestDelay**: This is how often the task should check if splash was passed
- **taskCount**: How many tasks to run
- **chromeDriverPath**: The location of your chromedriver file
- **useragent**: The useragent string to use for requests
- **selectors**: A list of HTML selectors to look for to verify that we passed splash.
- **onePass**: Whether you want the bot to stop tasks after one instance passes splash _[true/false]_
- **proxies**: The path to your proxies file _including_ file extension.