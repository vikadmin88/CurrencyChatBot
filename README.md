### CurrencyChatBot

#### How to run:

Rename the example config file `app.properties-example` to `app.properties` and configure it as follofs:

```
# Bot credentials:
APP_NAME=CurrencyChatBot
APP_BOT_NAME=here your bot name
APP_BOT_TOKEN=here your bot token

# User storage functionality:
# Enable/disable. save/load to/from json, database, etc.
APP_USERS_USE_STORAGE=true
# storage provider. file | sqlite
APP_USERS_STORAGE_PROVIDER=sqlite

# Default parameters for banks:
# Currency to get from each bank. separate by comma: USD,EUR,...
BANK_CURRENCY=USD,EUR,GBP,PLN
# Frequency of requests to banks for latest exchange rates (minutes)
BANK_FREQUENCY_REQUEST=1
# URLs of API bank pages
BANK_PB_API_URL=https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=5
BANK_MB_API_URL=https://api.monobank.ua/bank/currency
BANK_NBU_API_URL=https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json

# Default parameters for each new user
# only one (example: PB - Privat Bank, MB - Mono Bank, NBU - National Bank of Ukraine)
USER_DEF_BANK=PB
# only one of (USD | EUR | GBP | PLN)
USER_DEF_CURRENCY=USD
# Currency decimal places. One of: 2 | 3 | 4
USER_DEF_DECIMAL_PLACES=2

# Time to get exchange rates
# one of: 9 | 10 | 11 |...18
USER_DEF_NOTIFY_TIME=9
# Enable/disable One of: true | false
USER_DEF_NOTIFY_ENABLED=true
```

#### How to run Jar:

Compile: `gradle jar` FatJar will be saved to `./build/release/`

Copy `app.properties` to `./build/release/`

Run: `java -jar CurrencyChatBot.jar`

