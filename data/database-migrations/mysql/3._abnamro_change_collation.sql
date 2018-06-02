-- Fix collation on abnamro_buffered_accounts
ALTER TABLE `tink`.`abnamro_buffered_accounts`
DROP PRIMARY KEY;

ALTER TABLE `tink`.`abnamro_buffered_accounts`
CHANGE COLUMN `credentialsid` `credentialsid` VARCHAR(255) COLLATE 'utf8mb4_unicode_ci' NOT NULL;

ALTER TABLE `tink`.`abnamro_buffered_accounts`
ADD PRIMARY KEY (credentialsid(191), accountnumber);

-- Fix collation on abnamro_buffered_transactions

ALTER TABLE `tink`.`abnamro_buffered_transactions`
DROP PRIMARY KEY;

ALTER TABLE `tink`.`abnamro_buffered_transactions`
CHANGE COLUMN `credentialsid` `credentialsid` VARCHAR(255) COLLATE 'utf8mb4_unicode_ci' NOT NULL ,
CHANGE COLUMN `id` `id` VARCHAR(255) COLLATE 'utf8mb4_unicode_ci' NOT NULL ,
CHANGE COLUMN `cpaccount` `cpaccount` VARCHAR(255) COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL ,
CHANGE COLUMN `cpname` `cpname` VARCHAR(255) COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL ,
CHANGE COLUMN `type` `type` VARCHAR(255) COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL ;

ALTER TABLE `tink`.`abnamro_buffered_transactions`
ADD PRIMARY KEY(credentialsid(191), id(191));

