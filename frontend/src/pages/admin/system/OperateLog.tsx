import { Card, Table, Input, DatePicker, Space } from 'antd';

export default function AdminOperateLog() {
  return (
    <Card title="操作日志">
      <Space style={{ marginBottom: 16 }}>
        <Input.Search placeholder="搜索操作人/内容" style={{ width: 250 }} />
        <DatePicker.RangePicker />
      </Space>
      <Table columns={[
        { title: '时间', dataIndex: 'time', width: 170 },
        { title: '操作人', dataIndex: 'user', width: 100 },
        { title: '模块', dataIndex: 'module', width: 100 },
        { title: '操作内容', dataIndex: 'action' },
        { title: 'IP', dataIndex: 'ip', width: 130 },
        { title: '结果', dataIndex: 'result', width: 80 },
      ]} dataSource={[]} rowKey="id"
        locale={{ emptyText: '暂无日志记录' }} />
    </Card>
  );
}
