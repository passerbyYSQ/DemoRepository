import os

import opencc


def translate_properties(file_path):
    file_name = os.path.basename(file_path)
    converter = opencc.OpenCC('s2t.json')  # 加载简体到繁体的转换配置文件
    with open(file_path, 'r', encoding='utf-8') as file, open('res/' + file_name, 'w', encoding='utf-8') as res_file:
        for line in file:
            line = line.strip()
            if line and not line.startswith('#') and '=' in line:
                key, value = map(str.strip, line.split('=', 1))
                converted_text = converter.convert(value)
                res_file.write(f"{key}={str.strip(converted_text)}\n")


if __name__ == '__main__':
    translate_properties('props/PLF_messages_zh_CN.properties')
