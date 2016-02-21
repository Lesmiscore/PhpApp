
CREATE TEMPORARY TABLE tmp_user LIKE user;
INSERT INTO tmp_user VALUES ('%','root','','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','','','','',0,0,0,0,'','','N', 'N','', 0);
INSERT INTO tmp_user (host,user) VALUES ('%','');
INSERT INTO user SELECT * FROM tmp_user WHERE @had_user_table=0;
DROP TABLE tmp_user;
