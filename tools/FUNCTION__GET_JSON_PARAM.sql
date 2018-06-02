DROP FUNCTION IF EXISTS GET_JSON_PARAM;
DELIMITER $$
CREATE FUNCTION GET_JSON_PARAM (payload VARCHAR(255), param CHAR(25)) RETURNS VARCHAR(255)
BEGIN
	DECLARE val VARCHAR(255);
	DECLARE i_start int;
	DECLARE i_end int;
	SET i_start = LOCATE(param, payload);
	IF (i_start = 0) THEN
		SET val = NULL;
	ELSE
		SET i_start = i_start + LENGTH(param) + 3;
		SET i_end = LOCATE('"', payload, i_start);
		SET val = SUBSTR(payload, i_start, i_end - i_start);
	END IF;
	RETURN val;
END$$
DELIMITER ;

/*
-- Example: Get the id, description and TRANSFER_ACCOUNT value (from the serialized payload) for a transaction
SELECT
	t.id
	t.description,
	GET_JSON_PARAM(t.payload, 'TRANSFER_ACCOUNT') AS transfer_account,
FROM
	tink.transactions;
*/
