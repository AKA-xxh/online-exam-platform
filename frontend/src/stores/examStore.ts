import { create } from 'zustand';

interface ExamState {
  examStudentId: number;
  duration: number; // 分钟
  startTime: string;
  questions: any[];
  answers: Record<number, string>; // paperQuestionId -> answer JSON
  marked: Set<number>;              // 标记的题目
  currentIndex: number;

  init: (examStudentId: number, duration: number, startTime: string, questions: any[]) => void;
  setAnswer: (paperQuestionId: number, answer: string) => void;
  toggleMark: (paperQuestionId: number) => void;
  setCurrentIndex: (index: number) => void;
  reset: () => void;
}

export const useExamStore = create<ExamState>((set, get) => ({
  examStudentId: 0,
  duration: 0,
  startTime: '',
  questions: [],
  answers: {},
  marked: new Set<number>(),
  currentIndex: 0,

  init: (examStudentId, duration, startTime, questions) => set({
    examStudentId, duration, startTime, questions,
    answers: {}, marked: new Set<number>(), currentIndex: 0,
  }),

  setAnswer: (paperQuestionId, answer) => set(state => ({
    answers: { ...state.answers, [paperQuestionId]: answer },
  })),

  toggleMark: (paperQuestionId) => set(state => {
    const newMarked = new Set(state.marked);
    if (newMarked.has(paperQuestionId)) newMarked.delete(paperQuestionId);
    else newMarked.add(paperQuestionId);
    return { marked: newMarked };
  }),

  setCurrentIndex: (index) => set({ currentIndex: index }),

  reset: () => set({ examStudentId: 0, duration: 0, startTime: '', questions: [], answers: {}, marked: new Set(), currentIndex: 0 }),
}));
