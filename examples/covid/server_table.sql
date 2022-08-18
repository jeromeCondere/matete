

CREATE TABLE IF NOT EXISTS event_covid(
   experiment_no  varchar(50),
   country varchar(80),
   infected_count bigint,
   not_infected_count bigint,
   travellers bigint,
   ticks double precision
);


   