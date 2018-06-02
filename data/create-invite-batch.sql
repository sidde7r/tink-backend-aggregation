SELECT signup.email, signup.id, signup.rank_score + IFNULL(campaign.boost, 0) AS total_score
                FROM
                        (SELECT email, id, @score := @score + 1 AS rank_score
                        FROM
                                (SELECT @score := 0) r,
                                invite_signup
                                ORDER BY datetime DESC)
                        AS signup
                        LEFT JOIN
                                (SELECT signupId,
                                        SUM(CASE
                                                WHEN campaign = 'FB_LIKE' THEN 20
                                                WHEN campaign = 'STOKED' THEN 5 
                                                WHEN campaign = 'PNG' THEN -5000 
                                                ELSE 0 END) AS boost
                                        FROM invite_campaign
                                        GROUP BY signupId)
                                AS campaign
                                ON signup.id = campaign.signupId
                        LEFT JOIN invite_codes AS codes
                                ON signup.id = codes.signupId
                        LEFT JOIN users AS users
                                ON signup.email = users.username
                WHERE codes.code IS NULL AND users.username IS NULL AND codes.batch IS NULL
                ORDER BY total_score DESC
                LIMIT 100;