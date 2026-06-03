# Constants

Cross-component option lists and stable domain constants live here.

- `question.ts` - question type, difficulty, and enabled-state options reused by question and paper UI.
- `options.ts` - shared select options for domain filters such as difficulty, enabled state, submitted state, course status, paper status, and semesters.

Keep request functions in `src/api`, UI state in components or composables, and only shared static configuration in this folder.
