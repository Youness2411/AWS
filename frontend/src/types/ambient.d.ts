declare module 'marked' {
  export const marked: {
    parse: (src: string) => string;
  };
}

declare module 'dompurify' {
  const DOMPurify: {
    sanitize: (dirty: string) => string;
  };
  export default DOMPurify;
}

declare module '@toast-ui/editor' {
  export default class Editor {
    constructor(options: any);
    getMarkdown(): string;
    setMarkdown(markdown: string): void;
    on(event: string, handler: Function): void;
    addHook(name: string, hook: Function): void;
    destroy(): void;
  }
}


