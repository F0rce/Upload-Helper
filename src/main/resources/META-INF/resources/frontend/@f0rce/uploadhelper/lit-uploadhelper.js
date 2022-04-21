/**
@license MIT
Copyright 2021-2022 David "F0rce" Dodlek
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

import { LitElement, css } from "lit-element";

class LitUploadHelper extends LitElement {
  static get properties() {
    return {
      dropZone: { type: String, reflect: true },
      nodrop: { type: Boolean, reflect: true },
      target: { type: String },
      method: { type: String },
      timeout: { type: Number },
      _dragover: { type: Boolean },
      maxFiles: { type: Number },
      files: { type: Array },
      accept: { type: String },
      maxFileSize: { type: Number },
      _dragoverValid: { type: Boolean },
      fromDataName: { type: String },
      visualFeedback: { type: Boolean },
    };
  }

  constructor() {
    super();
    this.dropZone = "";
    this.nodrop = function () {
      try {
        return !!document.createEvent("TouchEvent");
      } catch (e) {
        return false;
      }
    };
    this.target = "";
    this.method = "POST";
    this.timeout = 0;
    this._dragover = false;
    this.maxFiles = 1;
    this.files = [];
    this.accept = "";
    this.maxFileSize = Infinity;
    this._dragoverValid = false;
    this.fromDataName = "file";
    this.visualFeedback = true;
  }

  static get styles() {
    return css``;
  }

  async firstUpdated(changedProperties) {
    this._size = ["B", "kB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"];

    this._dragoverListener = this._onDragover.bind(this);
    this._dragleaveListener = this._onDragleave.bind(this);
    this._dropListener = this._onDrop.bind(this);

    this.initializeUploadHelper();
  }

  updated(changedProperties) {
    changedProperties.forEach((oldValue, propName) => {
      let funcToCall = propName + "Changed";
      if (typeof this[funcToCall] == "function") {
        // This line if freaking epic
        this[funcToCall]();
      }
    });
  }

  initializeUploadHelper() {
    if (this.dropZone !== "") {
      if (this.dropZone.indexOf("|") > -1) {
        let elem = document.getElementById(this.dropZone.split("|")[0]);
        this.dropZoneElement = elem.shadowRoot.getElementById(
          this.dropZone.split("|")[1]
        );
      } else {
        this.dropZoneElement = document.getElementById(this.dropZone);
      }

      if (this.dropZoneElement == null) {
        return console.error("[Upload-Helper] dropZone ID not found.");
      }

      this.dropZoneElement.addEventListener("dragover", this._dragoverListener);
      this.dropZoneElement.addEventListener(
        "dragleave",
        this._dragleaveListener
      );
      this.dropZoneElement.addEventListener("drop", this._dropListener);
    }
  }

  dropZoneChanged() {
    if (this.dropZoneElement !== undefined) {
      this.dropZoneElement.removeEventListener(
        "dragover",
        this._dragoverListener
      );
      this.dropZoneElement.removeEventListener(
        "dragleave",
        this._dragleaveListener
      );
      this.dropZoneElement.removeEventListener("drop", this._dropListener);
    }

    if (this.dropZone !== "") {
      if (this.dropZone.indexOf("|") > -1) {
        let elem = document.getElementById(this.dropZone.split("|")[0]);
        this.dropZoneElement = elem.shadowRoot.getElementById(
          this.dropZone.split("|")[1]
        );
      } else {
        this.dropZoneElement = document.getElementById(this.dropZone);
      }

      if (this.dropZoneElement == null) {
        return console.error("[Upload-Helper] dropZone ID not found.");
      }

      this.dropZoneElement.addEventListener("dragover", this._dragoverListener);
      this.dropZoneElement.addEventListener(
        "dragleave",
        this._dragleaveListener
      );
      this.dropZoneElement.addEventListener("drop", this._dropListener);
    }
  }

  _onDragover(event) {
    event.preventDefault();
    if (this.visualFeedback) {
      this.dropZoneElement.classList.add("uh-on-hover");
    }
    if (!this.nodrop() && !this._dragover) {
      this._dragover = true;
    }
    if (event.dataTransfer.items.length > this.maxFiles) {
      event.dataTransfer.dropEffect = "none";
    } else {
      event.dataTransfer.dropEffect = this.nodrop() ? "none" : "copy";
    }
  }

  _onDragleave(event) {
    event.preventDefault();
    if (this.visualFeedback) {
      this.dropZoneElement.classList.remove("uh-on-hover");
    }
    if (this._dragover && !this.nodrop) {
      this._dragover = this._dragoverValid = false;
    }
  }

  _onDrop(event) {
    if (this.visualFeedback) {
      this.dropZoneElement.classList.remove("uh-on-hover");
    }
    if (!this.nodrop()) {
      event.preventDefault();
      this._dragover = this._dragoverValid = false;
      this._addFiles(event.dataTransfer.files);
    }
  }

  _addFiles(files) {
    Array.prototype.forEach.call(files, this._addFile.bind(this));
  }

  _addFile(file) {
    if (this.maxFileSize >= 0 && file.size > this.maxFileSize) {
      this.dispatchEvent(
        new CustomEvent("uh-file-reject", {
          detail: { file, error: "File is Too Big." },
        })
      );
      return;
    }
    const fileExt = file.name.match(/\.[^\.]*$|$/)[0];
    const re = new RegExp(
      "^(" + this.accept.replace(/[, ]+/g, "|").replace(/\/\*/g, "/.*") + ")$",
      "i"
    );
    if (this.accept && !(re.test(file.type) || re.test(fileExt))) {
      this.dispatchEvent(
        new CustomEvent("uh-file-reject", {
          detail: { file, error: "Incorrect File Type." },
        })
      );
      return;
    }
    file.loaded = 0;
    file.held = true;
    file.status = "Queued";
    this.files.unshift(file);

    this._uploadFile(file);
  }

  _createXhr() {
    return new XMLHttpRequest();
  }

  _configureXhr(xhr) {
    if (this.timeout) {
      xhr.timeout = this.timeout;
    }
  }

  _formatTime(split) {
    // Fill HH:MM:SS with leading zeros
    while (split.length < 3) {
      split.push(0);
    }

    return split
      .reverse()
      .map((number) => {
        return (number < 10 ? "0" : "") + number;
      })
      .join(":");
  }

  _formatSize(bytes) {
    // https://wiki.ubuntu.com/UnitsPolicy
    const base = 1000;
    const unit = ~~(Math.log(bytes) / Math.log(base));
    const dec = Math.max(0, Math.min(3, unit - 1));
    const size = parseFloat((bytes / Math.pow(base, unit)).toFixed(dec));
    return size + " " + this._size[unit];
  }

  _splitTimeByUnits(time) {
    const unitSizes = [60, 60, 24, Infinity];
    const timeValues = [0];

    for (var i = 0; i < unitSizes.length && time > 0; i++) {
      timeValues[i] = time % unitSizes[i];
      time = Math.floor(time / unitSizes[i]);
    }

    return timeValues;
  }

  _formatFileProgress(file) {
    return (
      file.totalStr +
      ": " +
      file.progress +
      "% (" +
      (file.loaded > 0
        ? "remaining time: " + file.remainingStr
        : "unknown remaining time") +
      ")"
    );
  }

  _setStatus(file, total, loaded, elapsed) {
    file.elapsed = elapsed;
    file.elapsedStr = this._formatTime(this._splitTimeByUnits(file.elapsed));
    file.remaining = Math.ceil(elapsed * (total / loaded - 1));
    file.remainingStr = this._formatTime(
      this._splitTimeByUnits(file.remaining)
    );
    file.speed = ~~(total / elapsed / 1024);
    file.totalStr = this._formatSize(total);
    file.loadedStr = this._formatSize(loaded);
    file.status = this._formatFileProgress(file);
  }

  _uploadFile(file) {
    if (file.uploading) {
      return;
    }

    const ini = Date.now();
    const xhr = (file.xhr = this._createXhr());

    let stalledId, last;
    // onprogress is called always after onreadystatechange
    xhr.upload.onprogress = (e) => {
      clearTimeout(stalledId);

      last = Date.now();
      const elapsed = (last - ini) / 1000;
      const loaded = e.loaded,
        total = e.total,
        progress = ~~((loaded / total) * 100);
      file.loaded = loaded;
      file.progress = progress;
      file.indeterminate = loaded <= 0 || loaded >= total;

      if (file.error) {
        file.indeterminate = file.status = undefined;
      } else if (!file.abort) {
        if (progress < 100) {
          this._setStatus(file, total, loaded, elapsed);
          stalledId = setTimeout(() => {
            file.status = "Stalled.";
          }, 2000);
        } else {
          file.loadedStr = file.totalStr;
          file.status = "Processing File...";
        }
      }

      this.dispatchEvent(
        new CustomEvent("uh-upload-progress", { detail: { file, xhr } })
      );
    };

    // More reliable than xhr.onload
    xhr.onreadystatechange = () => {
      if (xhr.readyState == 4) {
        clearTimeout(stalledId);
        file.indeterminate = file.uploading = false;
        if (file.abort) {
          return;
        }
        file.status = "";
        // Custom listener can modify the default behavior either
        // preventing default, changing the xhr, or setting the file error
        const evt = this.dispatchEvent(
          new CustomEvent("uh-upload-response", {
            detail: { file, xhr },
            cancelable: true,
          })
        );

        if (!evt) {
          return;
        }
        if (xhr.status === 0) {
          file.error = "Server Unavailable";
        } else if (xhr.status >= 500) {
          file.error = "Unexpected Server Error";
        } else if (xhr.status >= 400) {
          file.error = "Forbidden";
        }

        file.complete = !file.error;
        this.dispatchEvent(
          new CustomEvent(`uh-upload-${file.error ? "error" : "success"}`, {
            detail: { file, xhr },
          })
        );
      }
    };

    const formData = new FormData();

    file.uploadTarget = file.uploadTarget || this.target || "";
    file.formDataName = this.formDataName;

    const evt = this.dispatchEvent(
      new CustomEvent("uh-before-upload", {
        detail: { file, xhr },
        cancelable: true,
      })
    );
    if (!evt) {
      return;
    }

    formData.append(file.formDataName, file, file.name);

    xhr.open(this.method, file.uploadTarget, true);
    this._configureXhr(xhr);

    file.status = "Connecting...";
    file.uploading = file.indeterminate = true;
    file.complete = file.abort = file.error = file.held = false;

    xhr.upload.onloadstart = () => {
      this.dispatchEvent(
        new CustomEvent("uh-upload-start", {
          detail: { file, xhr },
        })
      );
    };

    // Custom listener could modify the xhr just before sending it
    // preventing default
    const uploadEvt = this.dispatchEvent(
      new CustomEvent("uh-upload-request", {
        detail: { file, xhr, formData },
        cancelable: true,
      })
    );
    if (uploadEvt) {
      xhr.send(formData);
    }
  }

  _dragoverChanged() {
    this._dragover
      ? this.setAttribute("dragover", this._dragover)
      : this.removeAttribute("dragover");
  }

  _dragoverValidChanged() {
    this._dragoverValid
      ? this.setAttribute("dragover-valid", this._dragoverValid)
      : this.removeAttribute("dragover-valid");
  }
}

customElements.define("lit-uploadhelper", LitUploadHelper);
