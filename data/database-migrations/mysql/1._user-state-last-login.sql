ALTER TABLE tink.users_states ADD COLUMN lastLogin DATETIME NULL DEFAULT NULL AFTER validPeriods;
