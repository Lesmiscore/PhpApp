! If you get error that android is not recognised for cross compile update config.sub and config.guess to newest versions

-	Build standalone android toolchain to /tmp/toolchain directory
- export PATH=$PATH:/tmp/toolchaing
- PCRE
	- Download pcre, cross compile and install it to /tmp/toolchain directory
	./configure --host=arm-linux-androideabi --prefix=/tmp/toolchain --disable-shared
- LibBind
	- edit include/resolv.h by changing "resolv.conf" path
	./configure --host=arm-linux-androideabi --prefix=/tmp/toolchain --disable-shared
- LibXml2
	./configure --host=arm-linux-androideabi --prefix=/tmp/toolchain --disable-shared
- LibIconv
	./configure --host=arm-linux-androideabi --prefix=/tmp/toolchain --disable-shared gl_cv_header_working_stdint_h=yes --enable-static
- LibXslt
	./configure --host=arm-linux-androideabi --prefix=/tmp/toolchain --disable-shared
- Bzip2
	- Edit make file setting correct compilers/lds and prefix
- OpenSSL
	- Get OpenSSL from https://github.com/guardianproject/openssl-android
	- ndk-build it
	- copy include/openssl to /tmp/toolchaing/include/openssl
	- copy libs/armeabi/*.so to /tmp/toolchaing/lib
	- Fix php source as needed
- ICU
	- Do non cross build of ICU to /home/esminis/Downloads/icu-build-linux	
	- Configure
		./configure --host=arm-linux-androideabi --prefix=/tmp/toolchain --with-cross-build=/home/esminis/Downloads/icu-build-linux ac_cv_func_nl_langinfo=no ac_cv_func_nl_langinfo=no --disable-shared --enable-static
	- Edit source code as needed
	- make
- PHP
	- Download php 5.4(tested on it) and extract
	- Download php 5.3 and copy ext/sqlite/ from it to php 5.4 ext/sqlite/, you can remove 5.3 now
	- cd into php 5.4 and run ./buildconf --force
	- Configure php with this command for cross compilation: 
		./configure --host=arm-linux-androideabi PCRECONFIG=/tmp/toolchain/bin/pcre-config --without-pear --disable-posix CFLAGS="-I/tmp/toolchain/include/bind" LDFLAGS="-liconv -L/tmp/toolchain/lib" LIBS="-licui18n -lstdc++ -lbind -L/tmp/toolchain/lib" --disable-cgi --with-pdo-mysql --with-mysqli --with-mysql --with-sqlite --with-iconv=/tmp/toolchain --with-libxml-dir=/tmp/toolchain --enable-mbstring --with-xsl=/tmp/toolchain --enable-soap --enable-sockets --enable-ftp --enable-bcmath --with-bz2 --enable-zip --with-zlib --with-openssl=/tmp/toolchain ac_cv_header_locale_h=no ac_cv_func_setlocale=no --enable-intl=static --with-icu-dir=/tmp/toolchain
	-	update main/php_config.h as needed, for example you can disable locale and setlocale there if not needed
	- fix errors in sqlite php files
	- make
	- sapi/cli/php file to android, for example: ./php -S localhost:8080 -t /sdcard/www

! Dont forget that usually /sdcard/ is mounted without exec permissions so you should copy php file to some directory with exec permissions for example /data/data/[your application directory]/php

! If you set "resolv.conf" as path then dont forget that php chmods to document directory, so if document directory is /sdcard/www then resolv.conf should be in /sdcard/www/resolv.conf

! Recommended filesystem layout:
/data/data/[your application directory]/php - php cli executable
/sdcard/www/ - root of webserver, put all your files here
/sdcard/www/resolv.conf - for DNS