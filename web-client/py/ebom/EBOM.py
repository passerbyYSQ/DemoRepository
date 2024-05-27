import collections
import csv
import re
import time
import uuid


"""
 迁移BOM结构 准备工作
1.BOMItemMaster迁移后的CSV文件 可通过Navicat导出
2.BOMItemRev迁移后的CSV文件 可通过Navicat导出
3.EBOM CSV文件 
 3.1 rel-b2b_EBOM.xls导入数据库
 3.2 通过Navicat导出csv文件
"""

""" BOMItemMater:{ID:obj}"""
master_id_obj = {}

""" BOMItemRev:{ID:[obj1,obj2...]}"""
rev_id_obj = collections.defaultdict(list)

b2b_ebom = list()


def read_bom_master_csv():
    """
     读取BOMItemMaster的csv文件
    :return:
    """

    csv_file_path = '../res/bomitemmaster.csv'
    with open(csv_file_path, 'r', encoding='utf-8') as file:
        reader = csv.DictReader(file)
        data = []
        for row in reader:
            data.append(row)
        print(len(data))
        for row in data:
            master_id_obj[row['ID']] = row


def read_bom_rev_csv():
    """
     读取BOMItemRev的csv文件
    :return:
    """
    csv_file_path = '../res/bomitemrev.csv'

    with open(csv_file_path, 'r', encoding='utf-8') as file:
        reader = csv.DictReader(file)
        data = []
        for row in reader:
            data.append(row)
        print(len(data))
        for row in data:
            rev_id_obj[row['ID']].append(row)


def read_ebom_csv():
    """
    读取EBOM的csv文件
    :return:
    """
    csv_file_path = '../res/rel-b2b_ebom.csv'

    with open(csv_file_path, 'r', encoding='utf-8') as file:
        reader = csv.DictReader(file)
        data = []
        for row in reader:
            data.append(row)
        print(len(data))
        for row in data:
            b2b_ebom.append(row)


def convert_number_to_letters(num: str):
    """
     数字转大版本字符串
    :param num:
    :return:
    """
    if not num:
        print(num)
        raise ValueError("num is none")
    if not re.match("^[0-9]+$", num):
        if num.isalpha():
            return num
        return 'A'
    number = int(num)
    if number == 0:
        number = 1
    if number < 1 or number > 26:
        raise ValueError("Number must be between 1 and 26.")
    ascii_code = number + 64
    letter = chr(ascii_code)
    return letter


def get_uuid():
    return uuid.uuid4()


def bom_source(production_make_buy_code) -> str:
    """

    :param production_make_buy_code:
    :return:
    """
    if not production_make_buy_code:
        return None

    if production_make_buy_code == "Unassigned":
        return "未分派"
    elif production_make_buy_code in ["Buy", "DesignBuy"]:
        return "外购件"
    elif production_make_buy_code == "VP":
        return "虚拟件"
    elif production_make_buy_code == "SP":
        return "标准件"
    elif production_make_buy_code == "Make":
        return "自制件"
    elif production_make_buy_code == "Customer":
        return "客户自带件"
    else:
        return "未分派"


def is_number(string):
    return string.isdigit()


work_unit = {"TZ-13", "TZ-12", "TZ-15", "TZ-14", "TZ-17", "TZ-16", "TZ-19", "TZ-18", "TZ-11", "TZ-10", "TZ-20", "TZ-22",
             "TZ-21", "ZQ-03", "ZQ-02", "ZQ-05", "ZQ-04", "ZQ-07", "ZQ-06", "ZQ-09", "ZQ-08", "ZQ-01", "ZF-03", "HQ-08",
             "ZF-04", "HQ-09", "ZF-05", "HQ-04", "HQ-05", "ZF-01", "HQ-06", "ZF-02", "HQ-07", "HQ-01", "HQ-02", "HQ-03",
             "TZ-02", "TZ-01", "TZ-04", "TZ-03", "TZ-06", "TZ-05", "TZ-08", "TZ-07", "DB-05", "DB-06", "DB-07", "DB-01",
             "ZQ-10", "DB-02", "DB-03", "DB-04", "ZQ-11", "HQ-11", "TZ-09", "HQ-10", "DP", "HB-02", "DQ-03", "HB-01",
             "DQ-02", "HB-04", "DQ-01", "HB-03", "DQ-07", "DQ-06", "DQ-05", "DQ-04", "ZJ-01", "ZJ-02", "HB-09", "HB-06",
             "WJ-06", "HB-05", "WJ-07", "HB-08", "WJ-08", "HB-07", "WJ-09", "WJ-10", "HB-11", "HB-10", "ZJ-12", "ZJ-13",
             "ZJ-10", "ZJ-11", "ZJ-09", "WJ-02", "WJ-03", "ZJ-07", "WJ-04", "ZJ-08", "WJ-05", "ZJ-05", "ZB-01", "ZJ-06",
             "ZB-02", "ZJ-03", "ZJ-04", "ZB-00", "WJ-01", "ZB-05", "ZB-06", "ZB-03", "DY-05", "ZB-04", "DY-04", "ZB-09",
             "DY-03", "DY-02", "ZB-07", "DY-01", "ZB-08", "DF-05", "DF-03", "DF-04", "DF-01", "DF-02", "ZJ-16", "ZB-12",
             "ZB-13", "ZJ-14", "ZB-10", "ZJ-15", "ZB-11", "ZB-14", "ZB-15"}


def get_work_unit(w_u: str):
    """
     返回合法的工位信息
    :param w_u:
    :return:
    """
    if not (w_u in work_unit):
        return ""
    return w_u


def convert2TimeStamp(date_string: str):
    """
    时间转换成时间戳字符串
    :param date_string:
    :return:
    """
    if not date_string:
        return str(int(time.time() * 1000))
    from datetime import datetime
    pattern = "%m/%d/%Y %I:%M:%S %p"
    try:
        date_object = datetime.strptime(date_string, pattern)
        timestamp = int(date_object.timestamp()) * 1000
        return timestamp
    except ValueError:
        print(date_string)
        exit(-1)


if __name__ == '__main__':
    errs = collections.defaultdict(list)
    sqls = []
    rVaultFold = []

    # 1.读取BOMMaster
    read_bom_master_csv()
    # 2.读取BOMItemRev
    read_bom_rev_csv()
    # 3.读取导出的EBOM
    read_ebom_csv()

    for b2b in b2b_ebom:
        left_id = b2b['fromname']
        left_revision = convert_number_to_letters(b2b['fromrevision'])
        left_sequence = '1'
        # 找到左侧对应的迁移后的uuid
        left_uuid = ''

        for rev in rev_id_obj[left_id]:
            if rev['Revision'] == left_revision:
                left_uuid = rev['UUID']

        if len(left_uuid) == 0:
            errs["左侧零部件缺失"].append(b2b)
            continue

        right_id = b2b['toname']
        if right_id not in master_id_obj:
            errs["右侧零部件缺失"].append(b2b)
            continue
        right_uuid = master_id_obj[right_id]['UUID']

        current_rev = convert_number_to_letters(b2b['torevision'])

        rev_master_sql = (
            f"INSERT INTO `rRevMaster`(`CurrentRevision`, `CurrentSequence`,`Unit`, `Qty`, `FindNumber`, `ViewNetworkID`, `IsPredecessorFrozen`, `ObjectKeyAttrMD5`, `ViewID`, `MarkedByCI`, `MarkedByOption`, `ReferenceDesignator`, `BOMLineNumber`, `AuthToolVersion`, `IsPlaced`, `Description`, `TransformMatrix`, `CADConfigName`, `AuthTool`, `IsInhibited`, `IsManagedByAuthTool`, `ComponentID`, `ReferenceType`, `ZXWorkUnit`, `ZXGroupLeader`, `BOMSource`, `Remark`, `LeftClass`, `RightClass`, `UUID_L`, `UUID_R`, `IsFrozen`, `UUID`, `Owner`, `Creator`, `CreateTimeStamp`, `ModifyTimeStamp`, `IsDepleted`) VALUES "
            f"('{current_rev}','1','EA', '{b2b['Quantity']}', '{b2b['FindNumber']}', '中兴智能多BOM视图管理', '-', '', '工程BOM', NULL, NULL, NULL, NULL, NULL, NULL,'{b2b['GYRemark']}', NULL, NULL, NULL, NULL, '-', NULL, NULL, '{get_work_unit(b2b['ProcessLocation'])}', '{b2b['ZTEFzOwner']}', '{bom_source(b2b['ProductionMakeBuyCode'])}', NULL, 'BOMItemRev', 'BOMItemMaster', '{left_uuid}', '{right_uuid}', '-', '{get_uuid()}', 'admin', 'Enovia', {convert2TimeStamp(b2b['StartEffectivityDate'])}, {convert2TimeStamp(b2b['StartEffectivityDate'])}, '-');\n")

        sqls.append(rev_master_sql)

    with open('rrevmaster.sql', 'w', encoding='utf-8') as file:
        file.writelines(sqls)

    for k, my_dict in errs.items():
        # 将字典写入CSV文件
        with open(k + '.csv', "w", newline="", encoding='utf-8') as file:
            writer = csv.DictWriter(file, fieldnames=my_dict[0].keys())
            writer.writeheader()  # 写入表头
            writer.writerows(my_dict)  # 写入字典数据
