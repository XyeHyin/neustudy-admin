# Components

Components are grouped by feature domain:

- `course/` - course creation and detail modals
- `grading/` - grading review UI
- `knowledge-point/` - knowledge point import/export/create/detail UI
- `paper/` - paper creation, detail, analysis, question management, smart generation
- `practice/` - practice session/detail/result UI
- `question/` - question create/detail/history/answer editor UI
- `role/` - role creation UI
- `user/` - user creation/detail UI
- `shared/` - application-wide shell components and icons

Keep feature-specific components inside their domain folder. Put only reusable cross-feature primitives in `shared/`.

Icons should be registered in `shared/icon.vue` and consumed through the exported `Icon` component. Do not import `@vicons/*` or render raw `n-icon` in feature views/components.
