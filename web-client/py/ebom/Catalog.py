import collections
import csv

from com.zx.zxt.ebom.BOMitemRev import now_time_str
from com.zx.zxt.ebom.BEOM import read_bom_master_csv, master_id_obj, get_uuid

name_prtctlg_obj = {}

"""
1.准备中兴环境的零部件分类csv 读取中兴环境的零部件分类prtctlg表 导出的csv文件
2.准备Envoia零部件分类参数转成的csv文件 可通过先导入数据库 再导出为csv文件
3.读取Envoia rel-b2b_Classified Item.xls转成的csv文件 可通过先导入数据库 再导出为csv文件
4.读取Envoia零部件bo_Part_ALL_ALL.xls转成的csv文件 可通过先导入数据库 再导出为csv文件 一部分分类参数在该表
"""

def read_prtctlg():
    """
    读取中兴环境的零部件分类prtctlg表 导出的csv文件
    :return:
    """
    with open('prtctlg.csv', 'r', encoding='utf-8') as file:
        reader = csv.DictReader(file)
        data = []
        for row in reader:
            data.append(row)
        for row in data:
            name_prtctlg_obj[row['Name']] = row


classified_name_obj = []

classified_set = set()


def read_classified():
    """
     读取rel-b2b_Classified Item.xls转成的csv文件 可通过先导入数据库 再导出为csv文件
    :return:
    """
    with open('relb2bclassifieditem.csv', 'r', encoding='utf-8-sig') as file:
        reader = csv.DictReader(file)
        data = []
        for row in reader:
            data.append(row)
        for row in data:
            if row['totype'].lower() != 'part':
                continue
            classified_name_obj.append(row)
            classified_set.add(row['fromname'])


# 中兴环境的分类和enovi导出的分类映射关系的补充
classified_catalog_mapping = {
    '3910 自动灭火系统（电气）': '3918自动灭火系统',
    '6100前乘客门总成': '6000乘客门总成',
    '6200中乘客门总成': '6000乘客门总成',
    '1309 风扇护风罩': '1309风扇护风罩',
    'Q617B 六角头螺塞': 'Q617B六角头螺塞',
    '6655BEVB': '6655',
    '6500司机门总成': '6400司机门总成',
    '3792翘板开关类': '3792翘板开关总成',
    'M02 板材': 'M02板材',
    'M01 型材': 'M01型材',
}

part_catalogs_params = collections.defaultdict(list)


def read_part_catalogs():
    """
    读取Envoia零部件分类参数转成的csv文件 可通过先导入数据库 再导出为csv文件
    :return:
    """
    with open('bo_part_catalog.csv', encoding='utf-8-sig') as file:
        reader = csv.DictReader(file)
        data = []
        for row in reader:
            data.append(row)
        for row in data:
            part_catalogs_params[row['name']].append(row)


bo_part_catalog = collections.defaultdict(list)


def read_part_all():
    """
    读取Envoia零部件bo_Part_ALL_ALL.xls转成的csv文件 可通过先导入数据库 再导出为csv文件
    一部分分类参数在该表
    :return:
    """
    with open('bo_part_all.csv', encoding='utf-8-sig') as file:
        reader = csv.DictReader(file)
        data = []
        for row in reader:
            data.append(row)
            bo_part_catalog[row['Name']].append(row)


if __name__ == '__main__':
    read_part_catalogs()
    read_prtctlg()
    read_classified()
    classified_catalog_obj = {}

    # 输出无法映射的分类 即Envoia导出的分类可能无法在中兴环境找到 可能是因为空格或另一种表述方式 eg. classified_catalog_mapping
    for key in classified_set: # enovia的分类
        if key in name_prtctlg_obj: # 中兴的分类
            classified_catalog_obj[key] = name_prtctlg_obj[key]
            continue
        if key in classified_catalog_mapping.keys():
            classified_catalog_obj[key] = name_prtctlg_obj[classified_catalog_mapping[key]]
            continue
        print(key)

    read_bom_master_csv()

    catalog_sqls = []
    update_rev_sql = []

    for b2b_class_item in classified_name_obj: # enovia的分类和业务对象的关系
        if b2b_class_item['totype'] != 'Part':
            continue
        id = b2b_class_item['toname']
        if id not in master_id_obj:
            continue
        master_uuid = master_id_obj[id]['UUID']
        catalog_name = b2b_class_item['fromname']
        if catalog_name not in classified_catalog_obj:
            continue
        catalog_obj = classified_catalog_obj[catalog_name] # 中兴分分类对象

        if catalog_obj['IsFinalNode'] == '-':
            continue
        upt = catalog_obj['UseParamTable']
        ptc = 'NnPrmPplt'
        if upt == '+':
            ptc = catalog_obj['ParamTableClass']
        update_sql = f"UPDATE `bomitemrev` SET CatalogPath = '{catalog_obj['Name']}' WHERE ID = '{id}';"
        update_master_sql = f"UPDATE `bomitemmaster` SET CatalogPath = '{catalog_obj['Name']}' WHERE ID = '{id}';"
        update_rev_sql.append(update_sql)
        update_rev_sql.append(update_master_sql)

        if ptc == 'NnPrmPplt':
            sql = f"INSERT INTO `{ptc}`(`LeftClass`, `RightClass`, `UUID_L`, `UUID_R`, `IsFrozen`, `UUID`, `Owner`, `Creator`, `CreateTimeStamp`, `ModifyTimeStamp`, `IsDepleted`) VALUES ('PrtCtlg', 'BOMItemMaster', '{catalog_obj['UUID']}', '{master_uuid}', '-', '{get_uuid()}', 'admin', 'Enovia', '{now_time_str()}', '{now_time_str()}', '-');"
            catalog_sqls.append(sql)
        elif ptc == 'SectionBarPrmtrzdPplt':
            # Envoia属性,中兴分类属性，默认值
            attr_mapping = [
                ('ZXMaterial', 'NoMaterial', ''),
                ('ZXSmallerEndCuttingDimensions', 'NoSmallEnd', ''),
                ('ZXElecProcessHoleIDCode', 'NoTechnique', ''),
                ('ZXSurfaceTreatment', 'NoSurface', ''),
                ('ZXCrossSectionalWidth', 'NoSectionWidth', ''),
                ('ZXSlantCuttingMethod', 'NoSlope', ''),
                ('ZXRawMaterialType', 'NoMaterialCategory', ''),
                ('ZXCrossSectionalLength', 'NoSectionHight', ''),
                ('ZXLargerEndCuttingDimensions', 'NoBigEnd', ''),
                ('ZXTotalLengthOfUsage', 'NoLength', ''),
                ('ZXCuttingSurface', 'NoSlopeAspect', ''),
                ('ZXWallThickness', 'NoThick', '')
            ]
            params_sql = ''
            params_val_sql = ''
            for attr, attr_map, dv in attr_mapping:
                params_sql += f"`{attr}`,"
                params_val_sql += f"'{dv}',"

            if id in part_catalogs_params.keys():
                params_val_sql = ''
                for attr, attr_map, dv in attr_mapping:
                    if attr_map and part_catalogs_params[id][0][attr_map]:
                        params_val_sql += f"'{part_catalogs_params[id][0][attr_map]}',"
                        continue
                    params_val_sql += f"'{dv}',"
            sql = f"INSERT INTO `{ptc}`({params_sql}`LeftClass`, `RightClass`, `UUID_L`, `UUID_R`, `IsFrozen`, `UUID`, `Owner`, `Creator`, `CreateTimeStamp`, `ModifyTimeStamp`, `IsDepleted`) VALUES ({params_val_sql}'PrtCtlg', 'BOMItemMaster', '{catalog_obj['UUID']}', '{master_uuid}', '-', '{get_uuid()}', 'admin', 'Enovia', '{now_time_str()}', '{now_time_str()}', '-');"
            catalog_sqls.append(sql)
        elif ptc == 'BusTypeParameterTable':
            ZXOrderID = ''
            ZXBUSModelSeries = ''
            if id in part_catalogs_params.keys():
                ZXBUSModelSeries = part_catalogs_params[id][0]['ZTESCCX']
            if id in bo_part_catalog:
                for e in bo_part_catalog[id]:
                    if e['ZTESCCX']:
                        ZXBUSModelSeries = e['ZTESCCX']
                        break
            sql = f"INSERT INTO `{ptc}`(`ZXOrderID`,`ZXBUSModelSeries`,`LeftClass`, `RightClass`, `UUID_L`, `UUID_R`, `IsFrozen`, `UUID`, `Owner`, `Creator`, `CreateTimeStamp`, `ModifyTimeStamp`, `IsDepleted`) VALUES ('{ZXOrderID}','{ZXBUSModelSeries}','PrtCtlg', 'BOMItemMaster', '{catalog_obj['UUID']}', '{master_uuid}', '-', '{get_uuid()}', 'admin', 'Enovia', '{now_time_str()}', '{now_time_str()}', '-');"
            catalog_sqls.append(sql)
        elif ptc == 'PlateMaterialParameterTable':
            # Envoia属性,中兴分类属性，默认值
            attr_mapping = [
                ('ZXMaterial', 'NoMaterial', ''),
                ('ZXLength', 'NoLength', ''),
                ('ZXSurfaceTreatment', 'NoSurface', ''),
                ('ZXPlateMaterialType', 'NoSheetCategory', ''),
                ('ZXWallThickness', 'NoThick', ''),
                ('ZXWidth', 'NoWidth', '')
            ]
            params_sql = ''
            params_val_sql = ''
            for attr, attr_map, dv in attr_mapping:
                params_sql += f"`{attr}`,"
                params_val_sql += f"'{dv}',"

            if id in part_catalogs_params.keys():
                params_val_sql = ''
                for attr, attr_map, dv in attr_mapping:
                    if attr_map and part_catalogs_params[id][0][attr_map]:
                        params_val_sql += f"'{part_catalogs_params[id][0][attr_map]}',"
                        continue
                    params_val_sql += f"'{dv}',"
            sql = f"INSERT INTO `{ptc}`({params_sql}`LeftClass`, `RightClass`, `UUID_L`, `UUID_R`, `IsFrozen`, `UUID`, `Owner`, `Creator`, `CreateTimeStamp`, `ModifyTimeStamp`, `IsDepleted`) VALUES ({params_val_sql}'PrtCtlg', 'BOMItemMaster', '{catalog_obj['UUID']}', '{master_uuid}', '-', '{get_uuid()}', 'admin', 'Enovia', '{now_time_str()}', '{now_time_str()}', '-');"
            catalog_sqls.append(sql)

    with open('catalogs.sql', 'w', encoding='utf-8') as f:
        f.write('\n'.join(catalog_sqls))

    # 更新零部件对象的分类信息
    with open('update_master.sql', 'w', encoding='utf-8') as f:
        f.write('\n'.join(update_rev_sql))
