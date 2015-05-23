<?php

error_reporting(E_ALL);
ini_set('display_errors', 1);
header('Content-Type: text/plain');

$modules = array('xdiff', 'xmldiff', 'stats', 'quickhash', 'id3', 'gender', 'bbcode', 'oauth', 'lzf', 'rar');

function validateXDiff() {
    return xdiff_string_diff("abcd\n", "abc\n", 1) !== false;
}

function validateXmlDiff() {
    return is_string((new XMLDiff\Memory())->diff('<a>a</a>', '<b>b</b>'));
}

function validateStats() {
    return is_float(stats_variance(array(1, 2, 3), false));
}

function validateQuickhash() {
    $set = new QuickHashIntSet( 1024, QuickHashIntSet::CHECK_FOR_DUPES);
    $set->add(3);
    return $set->exists(3);
}

function validateId3() {
    return is_int(id3_get_genre_id("Alternative"));
}

function validateGender() {
    $gender = new Gender\Gender; 
    return $gender->get("Milene", Gender\Gender::FRANCE) === Gender\Gender::IS_FEMALE;
}

function validateBBCode() {
    $handler = bbcode_create(
        array(
            '' => array('type'=>BBCODE_TYPE_ROOT,  'childs'=>'!i'),
            'b' => array('type'=>BBCODE_TYPE_NOARG, 'open_tag'=>'<b>', 'close_tag'=>'</b>')
        )
    );
    return bbcode_parse($handler, "[b]test[/b]") === '<b>test</b>';
}

function validateOAuth() {
    return is_string(oauth_get_sbs('get', 'http://esminis.com/'));
}

function validateLzf() {
    return !empty(lzf_optimized_for());
}

function validateRar() {
    return is_string(rar_wrapper_cache_stats());
}

foreach ($modules as $module) {
	$method = 'validate' . $module;
	var_dump($module . ': ' . ($method() ? "ok" : "error"));;
}



?>