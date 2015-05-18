<?php

$path = dirname(__FILE__) . '/test.sqlite';
$path = '/data/data/com.esminis.server.php/files/test.sqlite';

file_put_contents($path, '');
var_dump(is_file($path));
var_dump(chmod($path, 0777));
var_dump(is_executable($path));

odbc_connect('Driver=SQLite3;Database=' . $path, '', '', SQL_CUR_USE_ODBC);

//phpinfo(); ?>