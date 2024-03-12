def generate_delete_sql_script(file_name, table_names):
    with open('res/' + file_name, 'w', encoding='utf-8') as file:
        for table_name in table_names:
            file.write(f"DELETE FROM `{table_name}`;\n")


if __name__ == '__main__':
    generate_delete_sql_script("需要清空的业务数据表.sql", [
        'BOMItemRev', 'BOMItemMaster', 'DesignRev', 'DesignMaster', 'Document', 'ECN', 'ECR', 'rRevMaster', 'rRevision', 'rSuccessor',
        'CADModel', 'CADDrawing', 'GenFile', 'ExcelFile', 'WordFile', 'PPTFile', 'PDFFile', 'ZipFile', 'XMLFile',
        'rVaultFolder', 'rBiCntnts', 'rBmItmDsgn', 'rDsgnCntnts', 'rBomItmRvSpc', 'rChgCtrlAuth', 'rChgCtrlDlvr', 'rChgCtrlImpl',
        'LoginAccount', 'CoreUser', 'TaskList', 'Collector', 'CoreGroup', 'rGrpMmbr', 'Sign', 'rRlsAdmObjSig',
        'PrtCtlg', 'DesignCtlg', 'DocCtlg', 'ECNCtlg', 'ECRCtlg', 'NnPrmDvd',
        'NnPrmPplt', 'BusTypeParameterTable', 'SectionBarPrmtrzdPplt', 'PlateMaterialParameterTable', 'DocWrkInstrPrmtrzdPplt', 'ECNPrmtrzdPplt', 'ECRPrmtrzdPplt',
        'rLcEvt', 'LcEvtActnRcrd', 'LcAdvEvt', 'rTskLstLcEvt',
        'Supplier', 'ZXCMaterialSpecLib'
    ])
