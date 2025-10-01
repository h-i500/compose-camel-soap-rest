create table if not exists orders(
  id bigserial primary key,
  order_id varchar(50) not null,
  amount numeric(18,2) not null,
  created_at timestamp default now()
);
