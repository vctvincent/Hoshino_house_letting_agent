# Admin Dashboard Template Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the admin dashboard page from the supplied template structure while aligning its presentation with the existing agent workspace visual style.

**Architecture:** Keep the admin page self-contained in `src/views/admin/Admin.vue`, using one Vue SFC for state, computed summaries, ECharts rendering, and page-scoped styling. Reuse the provided admin template's module layout and data processing ideas, but restyle containers, cards, controls, and spacing to match the softer green dashboard language from the agent workspace.

**Tech Stack:** Vue 3 `script setup`, Vue Router, Axios request wrapper, ECharts, Vite

---

### Task 1: Rebuild the admin page shell

**Files:**
- Modify: `E:\Graduation_thesis\workspace\reams-frontend\src\views\admin\Admin.vue`

- [ ] Replace the empty admin page with the supplied template's section structure:
  hero, shortcut actions, alert strip, summary cards, trend chart, price-segment analysis, city comparison, regional distribution, leaderboards, status summary, and tips.

- [ ] Keep the page self-contained with local helpers and computed values instead of introducing new shared dependencies.

### Task 2: Align styles with the agent workspace

**Files:**
- Modify: `E:\Graduation_thesis\workspace\reams-frontend\src\views\admin\Admin.vue`
- Reference: `E:\Graduation_thesis\workspace\reams-frontend\src\views\agent\AgentWorkspace.vue`

- [ ] Restyle the admin page using the same background treatment, card surfaces, rounded corners, shadows, and button groups used by the agent workspace.

- [ ] Preserve the admin-specific information density from the supplied template while making spacing, typography, and interaction states visually consistent with the agent workspace.

### Task 3: Verify the page compiles

**Files:**
- Modify: `E:\Graduation_thesis\workspace\reams-frontend\src\views\admin\Admin.vue`

- [ ] Run `npm run build` in `E:\Graduation_thesis\workspace\reams-frontend`.

- [ ] Fix any compile-time issues from template migration, ECharts usage, or scoped styles until the build passes.
