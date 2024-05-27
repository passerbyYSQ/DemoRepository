import uuid
from collections import deque

import json
import time

import csv


def generate_delete_sql_script(file_name, table_names):
    with open('res/' + file_name, 'w', encoding='utf-8') as file:
        for table_name in table_names:
            file.write(f"DELETE FROM `{table_name}`;\n")


def generate_update_sql(file_name, table_name, attrs):
    #     update document set Creator='' where Creator=''
    # update document set ZXRspnsblUserName='' where ZXRspnsblUserName=''
    with open('csv/用户名映射.csv', 'r', encoding='utf-8') as map_file, open('res/' + file_name, 'w',
                                                                        encoding='utf-8') as file:
        reader = csv.reader(map_file, delimiter=',')
        rows = list(reader)[1:]
        for attr in attrs:
            for row in rows:
                file.write(f"UPDATE `{table_name}` SET `{attr}`='{row[1]}' WHERE `{attr}`='{row[0]}';\n")


def generate_DataItem_rVaultFolder(file_name):
    with open('csv/所有最新DataItem的UUID.csv', 'r', encoding='utf-8') as csv_file, open('res/' + file_name, 'w',
                                                                                    encoding='utf-8') as file:
        reader = csv.reader(csv_file, delimiter=',')
        rows = list(reader)[1:]
        timestamp = int(round(time.time() * 1000))
        for row in rows:
            file.write(
                f"DELETE FROM `rVaultFolder` WHERE `UUID_L`='5c75b699-0d20-49f9-9e8a-113497edc125' AND `UUID_R`='{row[0]}' AND `RightClass`='{row[1]}';\n")
            file.write(
                f"INSERT INTO `rVaultFolder`(`LeftClass`, `RightClass`, `UUID_L`, `UUID_R`, `IsFrozen`, `UUID`, `Owner`, `Creator`, `CreateTimeStamp`, `ModifyTimeStamp`, `IsDepleted`) VALUES ('VaultFolder', '{row[1]}', '5c75b699-0d20-49f9-9e8a-113497edc125', '{row[0]}', '-', UUID(), 'admin', 'enovia', '{timestamp}', '{timestamp}', '-');\n")


def generate_DataItem_MD5_update_sql(file_name):
    tables = ['CADModel', 'CADDrawing']
    with open('csv/file_md5.csv', 'r', encoding='utf-8') as csv_file, open('res/' + file_name, 'w',
                                                                           encoding='utf-8') as file:
        reader = csv.reader(csv_file, delimiter=',')
        rows = list(reader)[1:]
        # '文件名', 'MD5', '路径'
        for row in rows:
            for table in tables:
                file.write(
                    f"UPDATE {table} SET `MD5Value`='{row[1]}' WHERE `UUID`='{row[0]}' AND `MD5Value` IS NULL;\n")


def generate_relation_index_sql(file_name):
    with open('json/meta_tree.json', 'r', encoding='utf-8') as json_file, open('res/' + file_name, 'w',
                                                                               encoding='utf-8') as sql_file:
        data = json.loads(json_file.read())
        queue = deque([data])
        while queue:
            curr = queue.popleft()
            # 跳过抽象类或者动态类
            if not curr['isAbstract'] and not curr['isDynamic']:
                # 写入SQL
                sql_file.write(f"CREATE INDEX `index_left` ON `{curr['className']}` (`UUID_L`);\n")
                sql_file.write(f"CREATE INDEX `index_right` ON `{curr['className']}` (`UUID_R`);\n")
            # 持久化的关系类入队
            children = curr.get('children')
            if children:
                for child in children:
                    queue.append(child)


def generate_forbidden_rule(file_name):
    with open('res/' + file_name, 'w', encoding='utf-8') as sql_file:
        # 1. 操作主体：ParticipantClass, ParticipantID
        participant_list = [
            ('CoreGroup', '普通用户'),
            ('CoreGroup', '产品数据管理员'),
            ('CoreGroup', '系统管理员'),
        ]

        # 2. 数据类型
        data_class_list = ['BOMItemRev', 'DesignRev', 'Document']

        # 3. 操作行为：MessageName, IsMessageGroup
        message_list = [
            ('修订', '+')
        ]

        # 4. 状态条件
        condition_list = ['ZX_STATUS_NOT_PUBLISHED']

        timestamp = int(round(time.time() * 1000))

        for participant in participant_list:
            for data_class in data_class_list:
                for message in message_list:
                    for condition in condition_list:
                        sql_file.write(
                            f"INSERT INTO `ForbiddenRule`(`ParticipantID`, `ParticipantClass`, `ClassName`, `IsMessageGroup`, "
                            f"`MessageName`, `ConditionName`, `Description`, `OldCndtnName`, `PseudoCatalogName`, `ImpactChild`, "
                            f"`UUID`, `Owner`, `Creator`, `CreateTimeStamp`, `ModifyTimeStamp`, `IsDepleted`) VALUES ("
                            f"'{participant[1]}', '{participant[0]}', '{data_class}', '{message[1]}', '{message[0]}', '{condition}',"
                            f"NULL, NULL, NULL, '+', '{uuid.uuid4()}', 'admin', 'system', '{timestamp}', '{timestamp}', '-');\n")


if __name__ == '__main__':
    # generate_delete_sql_script("需要清空的业务数据表.sql", [
    #     'BOMItemRev', 'BOMItemMaster', 'DesignRev', 'DesignMaster', 'Document', 'ECN', 'ECR', 'rRevMaster', 'rRevision', 'rSuccessor',
    #     'CADModel', 'CADDrawing', 'GenFile', 'ExcelFile', 'WordFile', 'PPTFile', 'PDFFile', 'ZipFile', 'XMLFile',
    #     'rVaultFolder', 'rBiCntnts', 'rBmItmDsgn', 'rDsgnCntnts', 'rBomItmRvSpc', 'rChgCtrlAuth', 'rChgCtrlDlvr', 'rChgCtrlImpl',
    #     'LoginAccount', 'CoreUser', 'TaskList', 'Collector', 'CoreGroup', 'rGrpMmbr', 'Sign', 'rRlsAdmObjSig',
    #     'PrtCtlg', 'DesignCtlg', 'DocCtlg', 'ECNCtlg', 'ECRCtlg', 'NnPrmDvd',
    #     'NnPrmPplt', 'BusTypeParameterTable', 'SectionBarPrmtrzdPplt', 'PlateMaterialParameterTable', 'DocWrkInstrPrmtrzdPplt', 'ECNPrmtrzdPplt', 'ECRPrmtrzdPplt',
    #     'rLcEvt', 'LcEvtActnRcrd', 'LcAdvEvt', 'rTskLstLcEvt',
    #     'Supplier', 'ZXCMaterialSpecLib'
    # ])
    # generate_update_sql('update_user.sql', 'Document', ['Creator'])
    # generate_DataItem_rVaultFolder('补充最新DataItem的rVaultFolder关系.sql')
    # generate_DataItem_MD5_update_sql('填充模型文件的MD5值.sql')
    # generate_relation_index_sql('教育云端的关系索引.sql')
    generate_forbidden_rule('中兴的拒止规则.sql')
