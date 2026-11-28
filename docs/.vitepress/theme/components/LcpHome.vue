<script setup>
import { ref } from 'vue'
import { withBase } from 'vitepress'

const heroTab = ref('grid')
const featureTab = ref('vis')
const copied = ref(false)

const switchTabs = [
  {
    id: 'vis',
    title: 'Boundary culling',
    desc: 'Display entities spawn only within loaded chunks inside 64 blocks and clear across Folia regions.',
    label: 'VisualizationManager.java / limits',
    lines: [
      'DISPLAY_RANGE = 64 blocks around player',
      'Chunks filtered to world.isChunkLoaded(x, z)',
      'Per-player async generation token cancels stale tasks',
      'Switches cleanly between display_entities, particles, off'
    ]
  },
  {
    id: 'toggles',
    title: 'TNT and mobs',
    desc: 'Enable or lock TNT explosions and natural or spawner mob spawning inside every claim profile.',
    label: 'ClaimSettings.yml / runtime defaults',
    lines: [
      'tnt_enabled: false          # block primed TNT & minecarts',
      'mob_spawning_enabled: true  # natural, spawner, trial spawner',
      'pvp_enabled: false          # bypass when combat-tagged',
      'CoOwner role initializes 38 current V2 permission flags'
    ]
  },
  {
    id: 'wild',
    title: 'Permission chain',
    desc: 'Owner, role priority, per-player trust overrides, and visitor rules run cleanly on Paper and Folia.',
    label: 'PermissionResolver.java / order',
    lines: [
      '1. LandClaim admin bypass (landclaim.admin)',
      '2. Profile owner & CoOwner / custom role priority',
      '3. Individual trusted player overrides & ally flags',
      '4. Claim visitor settings or server-locked defaults'
    ]
  }
]

const commandRows = [
  { cmd: '/claim', scope: 'Claim chunk', note: 'Claims your current 16x16 chunk or active profile' },
  { cmd: '/claim menu settings', scope: 'Claim settings', note: 'Toggle PvP, titles, colors, TNT explosions, and mob spawning', highlight: true },
  { cmd: '/claim toggle <mode>', scope: 'Visualization', note: 'Switch between display_entities, particles, and off' },
  { cmd: '/claim ban <player>', scope: 'Moderation', note: 'Blocks claim entry at chunk borders across server restarts' },
  { cmd: '/claim admin <action>', scope: 'Administration', note: 'Supports console selectors and chunk allowance caps' }
]

const apiLines = [
  'LandClaimAPI api = LandClaimAPI.getInstance();',
  'ClaimProfile claim = api.getClaimAt(player.getLocation());',
  '',
  'if (claim != null) {',
  '    boolean tnt = claim.isTntEnabled();',
  '    boolean mobs = claim.isMobSpawningEnabled();',
  '    boolean allowed = api.hasPermission(claim, player.getUniqueId(), "BLOCK_BREAK");',
  '}'
]

function onTabKey(e, ids, setter) {
  const idx = ids.indexOf(e.currentTarget.dataset.tab)
  if (idx === -1) return
  if (e.key === 'ArrowRight') {
    e.preventDefault()
    setter(ids[(idx + 1) % ids.length])
  } else if (e.key === 'ArrowLeft') {
    e.preventDefault()
    setter(ids[(idx - 1 + ids.length) % ids.length])
  }
}

async function copyQuickCommand() {
  try {
    await navigator.clipboard.writeText('/claim menu settings')
    copied.value = true
    setTimeout(() => { copied.value = false }, 1800)
  } catch {
    copied.value = false
  }
}
</script>

<template>
  <div class="lcp-page">
    <section class="lcp-hero">
      <div class="lcp-container lcp-hero__grid">
        <div class="lcp-hero__copy">
          <h1 class="lcp-hero__title">Claim protection built for Paper</h1>
          <p class="lcp-hero__lead">
            Protect builds, custom roles, warps, TNT rules, and mob spawning on Paper.
          </p>
          <div class="lcp-actions">
            <a class="key key--primary" :href="withBase('/guide/installation')">Install plugin</a>
            <a class="key" :href="withBase('/guide/commands')">Commands</a>
          </div>
          <div class="lcp-cmd lcp-cut">
            <code>/claim menu settings</code>
            <button
              type="button"
              class="lcp-copy-btn"
              @click="copyQuickCommand"
            >
              <span v-if="!copied">Copy</span>
              <span v-else>Copied</span>
            </button>
            <span class="visually-hidden" aria-live="polite">{{ copied ? 'Command copied to clipboard' : '' }}</span>
          </div>
        </div>

        <aside class="lcp-cut lcp-graphite" aria-label="Claim boundary inspector">
          <div class="lcp-panel__head">
            <div class="lcp-panel__tabs" role="tablist" aria-label="Inspector views">
              <button
                type="button"
                role="tab"
                data-tab="grid"
                id="hero-tab-grid"
                class="lcp-tab"
                :aria-selected="heroTab === 'grid'"
                aria-controls="hero-panel-grid"
                :tabindex="heroTab === 'grid' ? 0 : -1"
                @click="heroTab = 'grid'"
                @keydown="onTabKey($event, ['grid', 'trace'], (v) => heroTab = v)"
              >Chunk grid</button>
              <button
                type="button"
                role="tab"
                data-tab="trace"
                id="hero-tab-trace"
                class="lcp-tab"
                :aria-selected="heroTab === 'trace'"
                aria-controls="hero-panel-trace"
                :tabindex="heroTab === 'trace' ? 0 : -1"
                @click="heroTab = 'trace'"
                @keydown="onTabKey($event, ['grid', 'trace'], (v) => heroTab = v)"
              >Permission trace</button>
            </div>
            <span class="lcp-panel__meta">Paper 26.2+ / Java 25</span>
          </div>

          <div
            v-show="heroTab === 'grid'"
            role="tabpanel"
            id="hero-panel-grid"
            aria-labelledby="hero-tab-grid"
            class="lcp-panel__body"
          >
<pre class="lcp-ascii" aria-label="ASCII diagram showing claimed chunks in cobalt beside wilderness cells"><span class="lcp-ascii__muted">..  ..  ..  ..  ..  ..  ..  ..</span>
<span class="lcp-ascii__muted">..  </span><span class="lcp-ascii__accent">[##][##]</span><span class="lcp-ascii__muted">  ..  </span>++  ++  <span class="lcp-ascii__muted">..</span>
<span class="lcp-ascii__muted">..  </span><span class="lcp-ascii__accent">[##][P ][##]</span> ++  ++  <span class="lcp-ascii__muted">..</span>
<span class="lcp-ascii__muted">..  </span><span class="lcp-ascii__accent">[##][##][##]</span><span class="lcp-ascii__muted">  ..  ..  ..</span></pre>
            <div class="lcp-legend">
              <span><span class="lcp-ascii__accent">[##]</span> Active claim   tnt: false</span>
              <span>++   Allied chunk   mobs: <span class="lcp-ascii__accent">true</span></span>
              <span>P    64m display    loaded only</span>
            </div>
          </div>

          <div
            v-show="heroTab === 'trace'"
            role="tabpanel"
            id="hero-panel-trace"
            aria-labelledby="hero-tab-trace"
            class="lcp-panel__body"
          >
            <ul class="lcp-trace">
              <li><span>Owner / admin</span><span class="lcp-trace__allow">Bypass</span></li>
              <li><span>CoOwner (priority 10)</span><span class="lcp-trace__allow">38 V2 flags</span></li>
              <li><span>Trusted player</span><span class="lcp-trace__allow">Explicit allow</span></li>
              <li><span>Banned player</span><span>Denied at border</span></li>
              <li><span>Visitor</span><span>Claim or locked default</span></li>
            </ul>
          </div>
        </aside>
      </div>
    </section>

    <section class="lcp-band lcp-band--tinted">
      <div class="lcp-container">
        <div class="lcp-section-head">
          <h2 class="lcp-section-title">Everything tuned per claim</h2>
          <p class="lcp-section-sub">Inspect boundary limits, protection toggles, and priority checks directly.</p>
        </div>

        <div class="lcp-showcase">
          <div class="lcp-switch-list" role="tablist" aria-label="System highlights">
            <button
              v-for="item in switchTabs"
              :key="item.id"
              type="button"
              role="tab"
              :data-tab="item.id"
              :id="'feat-tab-' + item.id"
              class="lcp-switch-btn"
              :aria-selected="featureTab === item.id"
              :aria-controls="'feat-panel-' + item.id"
              :tabindex="featureTab === item.id ? 0 : -1"
              @click="featureTab = item.id"
              @keydown="onTabKey($event, switchTabs.map(t => t.id), (v) => featureTab = v)"
            >{{ item.title }}</button>
          </div>

          <div
            v-for="item in switchTabs"
            v-show="featureTab === item.id"
            :key="'panel-' + item.id"
            role="tabpanel"
            :id="'feat-panel-' + item.id"
            :aria-labelledby="'feat-tab-' + item.id"
            class="lcp-cut lcp-graphite lcp-code-pane"
          >
            <div class="lcp-code-pane__head">
              <p class="lcp-code-pane__desc">{{ item.desc }}</p>
              <span class="lcp-code-pane__cap">{{ item.label }}</span>
            </div>
            <pre class="lcp-ascii"><template v-for="(line, i) in item.lines" :key="i"><span :class="i === 0 ? 'lcp-ascii__accent' : ''">{{ line }}</span>{{ '\n' }}</template></pre>
            <a class="key" :href="withBase('/CONFIGURATION')">Configuration</a>
          </div>
        </div>
      </div>
    </section>

    <section class="lcp-band">
      <div class="lcp-container">
        <div class="lcp-section-head">
          <h2 class="lcp-section-title">Commands players reach first</h2>
          <p class="lcp-section-sub">Backed by Cloud async command execution and optional LuckPerms scoping.</p>
        </div>

        <div class="lcp-table-wrap">
          <table class="lcp-table">
            <thead>
              <tr>
                <th scope="col">Command</th>
                <th scope="col">Area</th>
                <th scope="col">Behavior</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="row in commandRows"
                :key="row.cmd"
                :class="{ 'is-highlight': row.highlight }"
              >
                <td data-label="Command" class="lcp-mono">{{ row.cmd }}</td>
                <td data-label="Area">{{ row.scope }}</td>
                <td data-label="Behavior">{{ row.note }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>

    <section class="lcp-band lcp-band--tinted">
      <div class="lcp-container lcp-split">
        <div class="lcp-split__copy">
          <h2 class="lcp-section-title">Public Java API and events</h2>
          <p>
            Query claim locations, read profile flags, transfer ownership, or listen to claim and warp lifecycle events from external plugins and economy addons.
          </p>
          <a class="key" :href="withBase('/guide/api')">Java API docs</a>
        </div>

        <div class="lcp-cut lcp-graphite lcp-code-pane">
          <span class="lcp-code-pane__cap">LandClaimAPI.java / example</span>
          <pre class="lcp-ascii"><template v-for="(line, i) in apiLines" :key="i"><span :class="i < 2 ? 'lcp-ascii__accent' : ''">{{ line }}</span>{{ '\n' }}</template></pre>
        </div>
      </div>
    </section>
  </div>
</template>
