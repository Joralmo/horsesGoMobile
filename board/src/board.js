/*
 * Horses Go integration layer.
 * The host is authoritative: this file never calls Chessground.move() and
 * never paints a move before the host confirms it with renderBoard().
 */
(function () {
  'use strict';

  const FILES = 'abcdefgh';
  const LEVELS = new Set([4, 6, 8]);
  const frame = document.getElementById('board-frame');
  const groundElement = document.getElementById('ground');
  const cellGrid = document.getElementById('cell-grid');
  let ground = null;
  let state = null;
  let awaitingConfirmation = false;

  function keyFor(row, column) {
    return FILES[column] + String(row + 1);
  }

  function validKey(key, size) {
    return typeof key === 'string' && /^[a-h][1-8]$/.test(key)
      && FILES.indexOf(key[0]) < size && Number(key[1]) <= size;
  }

  function cellDescription(value) {
    return value === 1 ? 'casilla destruida' : value === 2 ? 'caballo negro'
      : value === 3 ? 'caballo blanco' : 'casilla libre';
  }

  function normalize(input) {
    if (!input || !Array.isArray(input.cells)) return null;
    const size = input.cells.length;
    if (!LEVELS.has(size) || input.cells.some(row => !Array.isArray(row) || row.length !== size)) return null;
    if (input.cells.some(row => row.some(value => typeof value !== 'number' || !Number.isInteger(value)))) return null;
    const cells = input.cells.map(row => row.slice());
    if (cells.some(row => row.some(value => ![0, 1, 2, 3].includes(value)))) return null;
    const legal = new Set(Array.isArray(input.legal) ? input.legal.filter(key => validKey(key, size)) : []);
    return { cells, size, legal, canPlay: input.canPlay === true };
  }

  function fenFor(cells, size) {
    const rows = [];
    for (let rank = 8; rank >= 1; rank -= 1) {
      const row = rank - 1;
      if (row >= size) {
        rows.push('8');
        continue;
      }
      let empty = 0;
      let fen = '';
      for (let column = 0; column < 8; column += 1) {
        const value = column < size ? cells[row][column] : 0;
        if (value === 0 || value === 1) {
          empty += 1;
        } else {
          if (empty) fen += String(empty);
          empty = 0;
          fen += value === 2 ? 'n' : 'N';
        }
      }
      if (empty) fen += String(empty);
      rows.push(fen);
    }
    return rows.join('/') + ' w - - 0 1';
  }

  function sendMove(key) {
    if (!state || !state.canPlay || awaitingConfirmation || !state.legal.has(key)) return;
    awaitingConfirmation = true;
    const ios = window.webkit && window.webkit.messageHandlers && window.webkit.messageHandlers.horsesGo;
    if (ios && typeof ios.postMessage === 'function') {
      ios.postMessage(key);
      return;
    }
    const android = window.HorsesGo;
    if (android && typeof android.play === 'function') {
      android.play(key);
      return;
    }
    awaitingConfirmation = false;
  }

  function setCellGrid(next) {
    cellGrid.replaceChildren();
    cellGrid.style.setProperty('--board-size', String(next.size));
    cellGrid.setAttribute('aria-label', `Tablero Horses Go ${next.size} por ${next.size}`);
    const fragment = document.createDocumentFragment();
    for (let row = next.size - 1; row >= 0; row -= 1) {
      const rank = document.createElement('div');
      rank.setAttribute('role', 'row');
      for (let column = 0; column < next.size; column += 1) {
        const key = keyFor(row, column);
        const value = next.cells[row][column];
        const cell = document.createElement('div');
        const legal = next.canPlay && next.legal.has(key);
        const edgeClasses = `${row === 0 ? ' file-edge' : ''}${column === 0 ? ' rank-edge' : ''}`;
        cell.className = `cell${value === 1 ? ' destroyed' : ''}${legal ? ' legal' : ''}${edgeClasses}`;
        cell.dataset.key = key;
        cell.dataset.file = FILES[column].toUpperCase();
        cell.dataset.rank = String(row + 1);
        cell.setAttribute('role', 'gridcell');
        cell.setAttribute('aria-label', `${key.toUpperCase()}, ${cellDescription(value)}${legal ? ', destino legal' : ''}`);
        cell.setAttribute('aria-disabled', legal ? 'false' : 'true');
        // Keep every map square reachable to VoiceOver/TalkBack; only legal
        // destinations receive pointer events and an action handler.
        cell.tabIndex = 0;
        if (legal) {
          cell.addEventListener('click', () => sendMove(key));
          cell.addEventListener('keydown', event => {
            if (event.key === 'Enter' || event.key === ' ') {
              event.preventDefault();
              sendMove(key);
            }
          });
        }
        rank.appendChild(cell);
      }
      fragment.appendChild(rank);
    }
    cellGrid.appendChild(fragment);
  }

  window.renderBoard = function renderBoard(input) {
    const next = normalize(input);
    if (!next) return false;
    state = next;
    awaitingConfirmation = false;
    const zoom = 8 / next.size;
    frame.style.setProperty('--board-size', String(next.size));
    frame.style.setProperty('--zoom', String(zoom));
    setCellGrid(next);
    const fen = fenFor(next.cells, next.size);
    if (!ground) {
      ground = Chessground(groundElement, {
        fen,
        orientation: 'white',
        coordinates: false,
        viewOnly: true,
        animation: { enabled: false },
      });
    } else {
      ground.set({ fen, viewOnly: true, orientation: 'white', animation: { enabled: false } });
    }
    return true;
  };

}());
