# Styles

Shared CSS files live here.

- `design-tokens.css` - semantic design tokens for the 4pt spacing scale, page widths, content padding, surface radius/shadow, and brand link color.
- `typography.css` - font stack, type scale, line-height, weight, and numeric typography defaults.
- `auth.css` - shared authentication page layout and variables.
- `admin-page.css` - shared structure styles for management/list views.
- `motion.css` - shared route, card, overlay, and control motion tokens and transitions.
- `scrollbar.css` - global scrollbar styling imported by the app entry.

Keep images and binary assets in `src/assets`, component-scoped styles inside their `.vue` files, and only reusable/global CSS in this folder.

Use semantic tokens before hard-coded values when styling shared page surfaces. Prefer `--space-*`, `--content-gap*`, and `--surface-*` for spacing and elevation. Prefer `admin-page`, `admin-card`, and `admin-title` for management views, and `auth-page`, `auth-card`, `auth-title`, and `auth-subtitle` for authentication views.
