use tink;
drop procedure if exists insert_local_development_crypto;
delimiter //


create procedure insert_local_development_crypto() 
begin
    set @cluster_id_crypto := (select clusterid from cluster_crypto_configurations where clusterid = 'local-development');
    -- Only insert rows if the platform was found
    if @cluster_id_crypto is null then 
        insert into cluster_crypto_configurations values ('local-development', 1, 'QUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUE=');
    end if;


    set @cluster_id := (select clusterid from cluster_host_configuration where clusterid = 'local-development');
    -- Only insert rows if the platform was found
    if @cluster_id is null then 
        insert into cluster_host_configuration values ('local-development', 'devtoken', '', false, 'http://127.0.0.1:5000');
        insert into cluster_provider_configurations (clusterid, providername) select 'local-development', name from provider_configurations;
    end if;
end;

//

delimiter ;
-- Execute the procedure
call insert_local_development_crypto();
-- Drop the procedure
drop procedure insert_local_development_crypto;




