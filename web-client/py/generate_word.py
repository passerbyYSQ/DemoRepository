from docx import Document




# 创建一个新的Word文档
doc = Document()

# 添加表格
num_rows = 4
num_cols = 3
table = doc.add_table(rows=num_rows, cols=num_cols)

# 填充表格内容
for row in range(num_rows):
    for col in range(num_cols):
        cell = table.cell(row, col)
        cell.text = f'Cell {row+1}-{col+1}'

# 合并单元格（从第1行第0列到第2行第1列的矩形区域）
table.cell(1, 0).merge(table.cell(3, 1))

# 保存文档
# doc.save('DocumentWithMergedCells.docx')

print('Word文档已生成！')
