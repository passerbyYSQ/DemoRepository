import collections
import csv
from typing import List

from ebom.BOMitemRev_rVaultFold import now_time_str, write_sql
from ebom.EBOM import get_uuid, convert_number_to_letters, rev_id_obj, read_bom_rev_csv

design_rev = collections.defaultdict(list)

design_rev_vault_rel = []

"""
处理零部件和设计图纸关系 设计图纸和仓库的关系
1.准备迁移后的零部件BOMItemRev表的csv文件
2.准备迁移后的设计方案的DesignRev的csv文件
3.Envoia rel-b2b_Part Specification.xls转成的csv文件 可通过先导入数据库 再导出为csv文件

"""


def read_design_rev():
    """
    读取迁移后的DesignRev的csv文件
    :return:
    """
    with open('../res/designrev.csv', 'r', encoding='utf-8') as file:
        reader = csv.DictReader(file)
        data = []
        for row in reader:
            data.append(dict(row))
        for row in data:
            design_rev[row['ID']].append(row)
            # 生成仓库设计对象关系
            rVaultFold_sql = f"INSERT INTO `rVaultFolder`(`LeftClass`, `RightClass`, `UUID_L`, `UUID_R`, `IsFrozen`, `UUID`, `Owner`, `Creator`, `CreateTimeStamp`, `ModifyTimeStamp`, `IsDepleted`) VALUES ('VaultFolder', 'DesignRev', '5c75b699-0d20-49f9-9e8a-113497edc125', '{row['UUID']}', '-', '{get_uuid()}', 'admin', 'Enovia', '{now_time_str()}', '{now_time_str()}', '-');\n"
            design_rev_vault_rel.append(rVaultFold_sql)


def read_bom_design() -> List:
    """
    读取Envoia rel-b2b_Part Specification.xls转成的csv文件 可通过先导入数据库 再导出为csv文件
    :return:
    """
    with open('relb2bpartspecification.csv', 'r', encoding='utf-8') as file:
        reader = csv.DictReader(file)
        data = []
        for row in reader:
            data.append(row)
        return data


if __name__ == '__main__':

    read_design_rev()
    read_bom_rev_csv()

    errs = collections.defaultdict(list)

    write_sql('dev_rVaultFold.sql', design_rev_vault_rel)

    sqls = []
    rels = read_bom_design()
    for rel in rels:
        left_id = rel['fromname']

        left_revision = convert_number_to_letters(rel['fromrevision'])
        left_sequence = '1'

        left_uuid = ''

        for rev in rev_id_obj[left_id]:
            if rev['Revision'] == left_revision:
                left_uuid = rev['UUID']

        if len(left_uuid) == 0:
            errs["左侧零部件缺失"].append(rel)
            continue

        right_id = rel['toname']
        right_revision = convert_number_to_letters(rel['torevision'])
        right_sequence = '1'

        right_uuid = ''

        for rev in design_rev[right_id]:
            if rev['Revision'] == right_revision:
                right_uuid = rev['UUID']

        if len(right_uuid) == 0:
            errs["右侧设计图纸缺失"].append(rel)
            continue

        rbmitmdsgn = f"INSERT INTO `rBmItmDsgn`(`IsMajor`, `LeftClass`, `RightClass`, `UUID_L`, `UUID_R`, `IsFrozen`, `UUID`, `Owner`, `Creator`, `CreateTimeStamp`, `ModifyTimeStamp`, `IsDepleted`) VALUES ('+', 'BOMItemRev', 'DesignRev', '{left_uuid}', '{right_uuid}', '-', '{get_uuid()}', 'admin', 'Enovia', '{now_time_str()}', '{now_time_str()}', '-');"

        sqls.append(rbmitmdsgn)

    with open('bom_design_rel.sql', 'w', encoding='utf-8') as f:
        f.write('\n'.join(sqls))

    for k, my_dict in errs.items():
        # 将字典写入CSV文件
        with open(k + '.csv', "w", newline="", encoding='utf-8') as file:
            writer = csv.DictWriter(file, fieldnames=my_dict[0].keys())

            writer.writeheader()  # 写入表头
            writer.writerows(my_dict)  # 写入字典数据
