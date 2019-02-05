CREATE TABLE `tink`.`abnamro_subscriptions` (
  `id` VARCHAR(255) NOT NULL,
  `userid` VARCHAR(255) NOT NULL,
  `subscriptionid` INT NOT NULL,
  `activationdate` DATETIME NULL,
  PRIMARY KEY (`id`));
