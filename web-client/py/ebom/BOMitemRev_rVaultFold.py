import time
from typing import List

from ebom.EBOM import read_bom_rev_csv, rev_id_obj, get_uuid


def now_time_str() -> str:
    return str(int(time.time() * 1000))


def write_sql(file_name: str, content: List[str]):
    with open(file_name, 'w', encoding='utf-8') as file:
        file.writelines(content)


if __name__ == '__main__':
    rVaultFold = []

    read_bom_rev_csv()

    for revs in rev_id_obj.values():
        for rev in revs:
            rVaultFold_sql = f"INSERT INTO `rVaultFolder`(`LeftClass`, `RightClass`, `UUID_L`, `UUID_R`, `IsFrozen`, `UUID`, `Owner`, `Creator`, `CreateTimeStamp`, `ModifyTimeStamp`, `IsDepleted`) VALUES ('VaultFolder', 'BOMItemRev', '4f973002-820c-b966-85f4-bc03253baa37', '{rev['UUID']}', '-', '{get_uuid()}', 'admin', 'Enovia', '{now_time_str()}', '{now_time_str()}', '-');\n"

            rVaultFold.append(rVaultFold_sql)

    write_sql('rVaultFold.sql', rVaultFold)
