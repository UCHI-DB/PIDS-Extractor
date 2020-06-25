from datamaran.dmmatcher import Pattern, dm_match_file, dm_match_line


def test_dm_match_line():
    pattern = Pattern('* * {* }\\n')
    assert (not dm_match_line('Food Processor', pattern, ['Food', 'Service', 'Establishment']))
    assert (dm_match_line('Food Service Establishment', pattern, ['Food', 'Service', 'Establishment']))


test_dm_match_line()
