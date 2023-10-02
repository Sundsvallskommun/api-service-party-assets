
	alter table asset modify asset_id varchar(255) NOT NULL;    	
	alter table asset modify issued date NOT NULL;    	
	alter table asset modify party_id varchar(255) NOT NULL;
	alter table asset modify `type` varchar(255) NOT NULL;
	alter table asset 
		add column party_type enum ('ENTERPRISE','PRIVATE') after party_id,
		add column origin varchar(255) after updated;
    	
