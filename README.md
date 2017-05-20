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
    <requestDelay>10000</requestDelay>
    <taskCount>1</taskCount>
    <chromeDriverPath>~/Downloads/chromedriver</chromeDriverPath>
    <useragent>
        Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36
    </useragent>
    <selectors>
        <selector>data-sitekey=</selector>
    </selectors>
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