import { readFile, writeFile } from 'node:fs/promises';
import { createRequire } from 'node:module';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { build } from 'esbuild';

const require = createRequire(import.meta.url);
const root = dirname(fileURLToPath(import.meta.url));
// The package's exports map intentionally hides package.json. Resolve its
// public entry point, then walk back from dist/ to the package root.
const packageRoot = dirname(dirname(require.resolve('@lichess-org/chessground')));
const read = file => readFile(join(root, file), 'utf8');

const [template, boardCss, boardJs, cgBase, cgBrown, cgCburnett, license] = await Promise.all([
  read('src/shell.html'),
  read('src/board.css'),
  read('src/board.js'),
  readFile(join(packageRoot, 'assets/chessground.base.css'), 'utf8'),
  readFile(join(packageRoot, 'assets/chessground.brown.css'), 'utf8'),
  readFile(join(packageRoot, 'assets/chessground.cburnett.css'), 'utf8'),
  readFile(join(packageRoot, 'LICENSE'), 'utf8'),
]);

// Bundle the official ESM entry and our adapter as one inline module. This
// keeps the runtime offline while avoiding references to minifier-local names.
const runtime = await build({
  stdin: {
    contents: `import { Chessground } from '@lichess-org/chessground';\n${boardJs}`,
    resolveDir: root,
    sourcefile: 'runtime.js',
    loader: 'js',
  },
  bundle: true,
  format: 'esm',
  minify: true,
  legalComments: 'none',
  write: false,
});
const offlineRuntime = runtime.outputFiles[0].text;
const licenseComment = license.replaceAll('--', '—');
const output = template
  .replace('@@CHESSGROUND_BASE@@', () => cgBase)
  .replace('@@CHESSGROUND_BROWN@@', () => cgBrown)
  .replace('@@CHESSGROUND_CBURNETT@@', () => cgCburnett)
  .replace('@@BOARD_CSS@@', () => boardCss)
  .replace('@@BOARD_RUNTIME@@', () => offlineRuntime)
  .replace('</head>', () => '<!-- Chessground 10.5.0, GPL-3.0-or-later. Cburnett pieces: Colin M. L. Burnett, GPLv2+. See THIRD_PARTY.md. -->\n</head>')
  .replace('</html>', () => `<!-- Full Chessground GPL-3.0-or-later license, reproduced from the pinned npm package.\n${licenseComment}\n-->\n</html>`);

await writeFile(join(root, '..', 'shared/src/commonMain/composeResources/files/board.html'), output);
console.log(`Wrote board.html (${Buffer.byteLength(output)} bytes)`);
