# Repbot

This repository contains the source code of the Repbot service bot, as deployed on the [First Internet Backgammon Server](https://en.wikipedia.org/wiki/First_Internet_Backgammon_Server). It was originally written by Dana Burns (burper) and Avi Kivity (avik) and released 2002 to the public domain on sourceforge. Since 2008 it was [maintained there](https://sourceforge.net/p/openfibs/code/HEAD/tree/trunk/modules/repbot) by Ingo Macherius (inim), and moved to github in 2018.

## Requirements

### Runtime
* a recent database server, tested with MariaDB 10.1, 
* Java 8 or better

### Build and IDE
* [lombok](https://github.com/rzwitserloot/lombok)
* [dagger](https://github.com/google/dagger)
* maven 3.0 or better

Please note that you typically have to [install lombok into your IDE](https://projectlombok.org/setup/overview) to work with the source code. Maven takes care of the code generation with dagger.

## Contact

inimfibs@gmail.com