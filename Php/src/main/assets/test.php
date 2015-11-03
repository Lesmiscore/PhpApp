<?php

error_reporting(E_ALL);
ini_set('display_errors', 1);
header('Content-Type: text/plain');

$modules = array(
    'xdiff', 'xmldiff', 'stats', 'quickhash', 'id3', 'gender', 'bbcode', 'oauth', 'lzf', 'rar',
    'rpmreader', 'eio', 'yaml', 'mailparse', 'spltypes', 'judy'
);

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

function validateRpmReader() {
		return rpm_is_valid(
			'ftp://ftp.pbone.net/mirror/dl.central.org/dl/openafs/1.6.14/rhel5/i386/dkms-openafs-1.6.14-1.el5.i386.rpm'
		) && !rpm_is_valid('http://rpm.pbone.net/');
}

function validateEio() {
	global $__eio_valid__;
	$__eio_valid__ = false;
	$tmp_filename = "eio-file.tmp";
	touch($tmp_filename);

	eio_stat($tmp_filename, EIO_PRI_DEFAULT, "validateEioStat", "eio_stat");
	eio_open($tmp_filename, EIO_O_RDONLY, NULL, EIO_PRI_DEFAULT, "validateEioOpen", $tmp_filename);
	eio_event_loop();
	return $__eio_valid__;
}

function validateEioStat($data, $result) {
	if (!empty($result)) {
		global $__eio_valid__;
		$__eio_valid__ = true;
	}
}

function validateEioOpen($data, $result) {
	eio_close($result);
	eio_event_loop();
	@unlink($data);
}

function validateYaml() {
  return yaml_parse("---\ntest: 34843")['test'] === 34843;
}

function validateMailParse() {
	return mailparse_msg_free(mailparse_msg_create());
}

function validateSplTypes() {
	return (string)new SplInt(94) === "94";
}

function validateJudy() {
	$judy = new Judy(Judy::STRING_TO_MIXED);
	$judy["foo"] = "bar";
	$valid = $judy->first() === "foo" && $judy["foo"] === "bar";
	$judy->free();
	return $valid;
}

foreach ($modules as $module) {
	$method = 'validate' . $module;
	var_dump($module . ': ' . ($method() ? "ok" : "error"));;
}

?>