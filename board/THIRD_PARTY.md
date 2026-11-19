# Third-party notices

This board embeds **Chessground 10.5.0** from the official Lichess package
`@lichess-org/chessground`, distributed under **GPL-3.0-or-later**.

- Repository: https://github.com/lichess-org/chessground
- Package metadata: https://registry.npmjs.org/@lichess-org/chessground/-/chessground-10.5.0.tgz
- Version: `10.5.0`
- Included resources: `dist/chessground.min.js`, `assets/chessground.base.css`,
  `assets/chessground.brown.css`, and `assets/chessground.cburnett.css`.
- The Cburnett piece set is credited to Colin M. L. Burnett and is distributed
  under GPLv2+ in the Lichess attribution notice:
  https://github.com/lichess-org/lila/blob/master/COPYING.md
- License text: `CHESSGROUND-LICENSE` (copied verbatim from the package);
  the generated `board.html` also carries the complete license in its source
  comment.

`board.html` is the corresponding offline source artifact generated from this
directory and the lockfile. No network request is made by the runtime page.
