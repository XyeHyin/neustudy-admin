import type { PermissionVO } from '@/api/types'

export function buildPermissionTree(perms: PermissionVO[]) {
  const moduleOrder = ['dashboard', 'user', 'role', 'permission', 'category', 'product', 'order', 'file']
  const groups: Record<string, PermissionVO[]> = {}
  perms.forEach(p => {
    const [module] = p.code.split(':')
    if (!groups[module]) groups[module] = []
    groups[module].push(p)
  })
  const sortedModules = Object.keys(groups).sort((a, b) => {
    const ia = moduleOrder.indexOf(a)
    const ib = moduleOrder.indexOf(b)
    if (ia === -1 && ib === -1) return a.localeCompare(b)
    if (ia === -1) return 1
    if (ib === -1) return -1
    return ia - ib
  })
  return sortedModules.map(module => ({
    label:
      module === 'dashboard'
        ? '仪表盘'
        : module === 'user'
          ? '用户管理'
          : module === 'role'
            ? '角色管理'
            : module === 'permission'
              ? '权限管理'
              : module === 'category'
                ? '分类管理'
                : module === 'product'
                  ? '课程管理'
                  : module === 'order'
                    ? '题目管理'
                    : module === 'file'
                      ? '文件管理'
                      : module === 'ai'
                        ? 'AI 管理'
                        : module === 'course'
                          ? '课程管理'
                          : module === 'practice'
                            ? '练习管理'
                            : module === 'knowledge_point'
                              ? '知识点管理'
                              : module === 'paper'
                                ? '试卷管理'
                                : module === 'question'
                                  ? '题目管理'
                                  : module === 'statistics'
                                    ? '统计管理'
                                    : module === 'grading'
                                      ? '判分管理'
                                      : '其他权限',
    key: module,
    children: buildChildren(groups[module], module),
    selectable: false
  }))
}

function buildChildren(perms: PermissionVO[], module?: string) {
  const secondGroups: Record<string, PermissionVO[]> = {}
  perms.forEach(p => {
    const parts = p.code.split(':')
    const second = parts[1] || '仪表盘'
    if (!secondGroups[second]) secondGroups[second] = []
    secondGroups[second].push(p)
  })
  const secondOrder = ['list', 'view', 'create', 'edit', 'delete', 'assign', 'update', 'upload']
  const sortedSeconds = Object.keys(secondGroups).sort((a, b) => {
    const ia = secondOrder.indexOf(a)
    const ib = secondOrder.indexOf(b)
    if (ia === -1 && ib === -1) return a.localeCompare(b)
    if (ia === -1) return 1
    if (ib === -1) return -1
    return ia - ib
  })
  const children: any[] = []
  sortedSeconds.forEach(second => {
    const items = secondGroups[second]
    if (items.length > 1) {
      children.push({
        label:
          second === 'upload'
            ? '上传'
            : second === 'list'
              ? '列表'
              : second === 'view'
                ? '查看'
                : second === 'create'
                  ? '新增'
                  : second === 'edit'
                    ? '编辑'
                    : second === 'delete'
                      ? '删除'
                      : second === 'assign'
                        ? '分配'
                        : second === 'update'
                          ? '修改'
                          : second === 'publish'
                            ? '发布'
                            : second === 'statistics'
                              ? '统计'
                              : second === 'answer'
                                ? '答题'
                                : second === 'config'
                                  ? '配置'
                                  : second === 'record'
                                    ? '记录'
                                    : second === 'archive'
                                      ? '归档'
                                      : second === 'question'
                                        ? '题目'
                                        : '仪表盘',
        key: module ? `${module}-${second}` : second,
        children: items.map(p => ({
          label: `${p.name}（${p.code}）`,
          key: p.id // 关键：叶子节点key为字符串
        }))
      })
    } else {
      children.push({
        label: `${items[0].name}（${items[0].code}）`,
        key: items[0].id // 关键：叶子节点key为字符串
      })
    }
  })
  return children
}
