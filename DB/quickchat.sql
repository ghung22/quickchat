use master
go
if db_id('Quickchat') is not null
    drop database Quickchat
go
create database Quickchat
go
use Quickchat
go

create table UserData
(
    UserName varchar(64),
    UserPassword varchar(64),
    UserProfile varchar(256),

    constraint PK_UserName
    primary key (UserName)
)
go

insert into UserData
    (UserName, UserPassword, UserProfile)
values
    ('nate', '1234', 'Data/default-profile.png'),
    ('tester', '4321', 'Data/default-profile.png'),
    ('hello', '2468', 'Data/default-profile.png')
go