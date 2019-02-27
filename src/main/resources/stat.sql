select
    count(*) count,
    max(diff_time) max,
    min(diff_time) min,
    avg(diff_time) avg,
    (select count(*) from statistics t where t.success = true) success,
    (select count(*) from statistics t where t.error = true) error,
    (select diff_time from statistics where id = ((80 * (select count(*) from statistics) + 1) / 100) order by diff_time) p80,
    (select diff_time from statistics where id = ((85 * (select count(*) from statistics) + 1) / 100) order by diff_time) p85,
    (select diff_time from statistics where id = ((90 * (select count(*) from statistics) + 1) / 100) order by diff_time) p90,
    (select diff_time from statistics where id = ((95 * (select count(*) from statistics) + 1) / 100) order by diff_time) p95,
    (select diff_time from statistics where id = ((99 * (select count(*) from statistics) + 1) / 100) order by diff_time) p99
from statistics order by id
