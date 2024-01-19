import os
import random
import time

# from googletrans import Translator
# https://github.com/foyoux/pygtrans
from pygtrans import Translate


# 先开启科学上网
# 启动配置必须设置代理 https://www.jianshu.com/p/6ff8082e1673 代理环境变量 HTTP_PROXY=http://127.0.0.1:26001;HTTPS_PROXY=http://127.0.0.1:26001;ALL_PROXY=http://127.0.0.1:26001
# https://blog.csdn.net/xfyuanjun520/article/details/115465873 Google翻译更改了API导致，googletrans版本为3.0.0，解析返回包失败，必须使用 googletrans 4.0.0+
# 较为优质的一篇踩坑资料：https://blog.csdn.net/qq_45668594/article/details/131967904

def translate(text: list, lang):
    # translator = Translator(timeout=Timeout(30))
    # translated = translator.translate(text, dest=lang)
    client = Translate(proxies={'https': 'http://localhost:26001'}, timeout=16)
    translated = client.translate(text, target=lang)
    sleep_time = random.randint(200, 400) / 1000
    time.sleep(sleep_time)  # 随机睡眠200~400 ms
    return [t.translatedText for t in translated]


def translate_properties(file_path, batch_limit, lang):
    file_name = os.path.basename(file_path)
    with open(file_path, 'r', encoding='utf-8') as file, open('res/' + file_name, 'w', encoding='utf-8') as res_file:
        batch_list = []
        char_count = 0
        for line in file:
            line = line.strip()
            if line and not line.startswith('#') and '=' in line:
                key, value = map(str.strip, line.split('=', 1))
                char_count += len(value)
                if char_count > batch_limit:  # 超出限制
                    translate_write(batch_list, lang, res_file)
                    batch_list.clear()
                    char_count = len(value)
                batch_list.append({'key': key, 'value': value})
        if char_count > 0:
            translate_write(batch_list, lang, res_file)


def translate_write(batch_list, lang, res_file):
    values = [entry['value'] for entry in batch_list]
    # delimiter = '|'  # 攒够一批次翻译，分隔符影响上下文，使用 | 能够隔离句意
    # text = delimiter.join(values)  # 待翻译的文本
    translated_text = translate(values, lang)
    # translated_values = translated_text.split(delimiter)
    for (index, entry) in enumerate(batch_list):
        res_file.write(f"{entry['key']}={str.strip(translated_text[index])}\n")


# 调用频率限制。攒够5000字再去翻译
# 随机时间间隔
if __name__ == '__main__':
    translate_properties('props/WEB_messages_zh_CN.properties', 1000, 'zh-TW')
