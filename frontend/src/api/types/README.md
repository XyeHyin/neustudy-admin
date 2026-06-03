# API Types

Types are grouped by backend feature domain and re-exported from `index.ts`.

Importing from `@/api/types` remains the public frontend entry. Keep new DTO/VO interfaces in the matching feature file instead of adding another large aggregate file.

- `common.ts`: shared response and pagination wrappers
- `auth.ts`, `user.ts`, `role.ts`: identity and system administration
- `category.ts`, `course.ts`, `knowledge-point.ts`: learning domain
- `question.ts`, `paper.ts`, `practice.ts`, `grading.ts`: assessment domain
