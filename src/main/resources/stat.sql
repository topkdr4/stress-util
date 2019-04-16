select
    count(*) count,
    (select count(*) from t_statistics where success = 1) success,
    (select max(a.diff_time) from (
        select diff_time
        from t_statistics
        where success = 1
        order by diff_time
        limit (select 80 * ((select (count(*) + 1) / 100 from t_statistics where success = 1)))) A
    ) p80,
    (select max(a.diff_time) from (
        select diff_time
        from t_statistics
        where success = 1
        order by diff_time
        limit (select 90 * ((select (count(*) + 1) / 100 from t_statistics where success = 1)))) A
    ) p90,
    (select max(a.diff_time) from (
        select diff_time
        from t_statistics
        where success = 1
        order by diff_time
        limit (select 99 * ((select (count(*) + 1) / 100 from t_statistics where success = 1)))) A
    ) p99
from t_statistics
