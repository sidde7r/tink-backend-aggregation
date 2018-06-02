CREATE TABLE `abnamro_buffered_accounts` (
  `accountnumber` bigint(20) NOT NULL,
  `credentialsid` varchar(255) NOT NULL,
  `complete` bit(1) NOT NULL,
  `transactioncount` int(11) NOT NULL,
  PRIMARY KEY (`accountnumber`,`credentialsid`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `abnamro_buffered_transactions` (
  `id` varchar(255) NOT NULL,
  `accountnumber` bigint(20) NOT NULL,
  `amount` double NOT NULL,
  `cpaccount` varchar(255) DEFAULT NULL,
  `cpname` varchar(255) DEFAULT NULL,
  `credentialsid` varchar(255) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `description` text,
  `payload` text,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`(191)),
  KEY `credentialsid_accountnumber` (`credentialsid`(191),`accountnumber`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
