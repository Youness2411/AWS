import { CurrencyPipe } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { EventEmitter, Injectable } from '@angular/core';
import CryptoJs from "crypto-js";
import { Observable } from 'rxjs';


@Injectable({
  providedIn: 'root'
})

export class Api {
  
  private static BASE_URL = 'https://one-piece-theory-evaluator.onrender.com/api';
  private static ENCRYPTION_KEY = "my-encryption-key";
  
  authStatusChange = new EventEmitter<void>();

  constructor(private http: HttpClient){}

  encryptAndSaveToStorage(key:string, value:string):void{
    const encryptedValue = CryptoJs.AES.encrypt(value, Api.ENCRYPTION_KEY).toString();
    localStorage.setItem(key, encryptedValue);

    
  }

  private getFromStorageAndDecrypt(key:string): any {
    try {
      const encryptedValue = localStorage.getItem(key);
      if (!encryptedValue) return null
      return CryptoJs.AES.decrypt(encryptedValue, Api.ENCRYPTION_KEY).toString(CryptoJs.enc.Utf8);
   
    } catch (error) {
      return null;
    }
  }

  private clearAuth(){
    localStorage.removeItem("token");
    localStorage.removeItem("role");
  }

  private getHeader(): HttpHeaders {
    const token = this.getFromStorageAndDecrypt("token");
    return new HttpHeaders({
      Authorization: `Bearer ${token}`
    })
  }
  
  
  // AUTH & USERS API METHODS
  registerUser(body: any): Observable<any>{
    return this.http.post(`${Api.BASE_URL}/auth/register`, body);
  }

  loginUser(body: any): Observable<any>{
    return this.http.post(`${Api.BASE_URL}/auth/login`, body);
  }

  getLoggedInUserInfo(): Observable<any>{
    return this.http.get(`${Api.BASE_URL}/users/current`, {
      headers: this.getHeader(),
    });
  }
  getAllUsers(): Observable<any>{
    return this.http.get(`${Api.BASE_URL}/users/all`, {
      headers: this.getHeader(),
    });
  }
  updateUser(id: string, formData: any): Observable<any>{
    return this.http.put(`${Api.BASE_URL}/users/update/${id}`, formData, {
      headers: this.getHeader(),
    });
  }
  updateUserRole(id: string, role: 'USER'|'MODERATOR'|'ADMIN'): Observable<any>{
    return this.http.put(`${Api.BASE_URL}/users/role/${id}?role=${role}`, {}, {
      headers: this.getHeader(),
    });
  }


  /**THEORIES ENDPOINTS */
  postTheory(formData: any): Observable<any> {
    return this.http.post(`${Api.BASE_URL}/theories/post`, formData, {
      headers: this.getHeader(),
    });
  }

  getAllTheories(page?: number, size?: number): Observable<any>{
    const params = [];
    if (page !== undefined) params.push(`page=${page}`);
    if (size !== undefined) params.push(`size=${size}`);
    const queryString = params.length > 0 ? `?${params.join('&')}` : '';
    return this.http.get(`${Api.BASE_URL}/theories/all${queryString}`,{
      headers: this.getHeader(),
    });
  }

  getAllTheoriesFlagged(): Observable<any>{
    return this.http.get(`${Api.BASE_URL}/theories/flagged`, {
      headers: this.getHeader(),
    });
  }

  queryTheories(params: { q?: string; sort?: 'date'|'mostLiked'|'mostComments'|'mostVotes'; page?: number; size?: number }): Observable<any>{
    const queryParams = [];
    if (params?.q) queryParams.push(`q=${encodeURIComponent(params.q)}`);
    if (params?.sort) queryParams.push(`sort=${encodeURIComponent(params.sort)}`);
    if (params?.page !== undefined) queryParams.push(`page=${params.page}`);
    if (params?.size !== undefined) queryParams.push(`size=${params.size}`);
    const queryString = queryParams.length > 0 ? `?${queryParams.join('&')}` : '';
    return this.http.get(`${Api.BASE_URL}/theories/query${queryString}`);
  }

  getTrendingTheories(limit: number = 10): Observable<any>{
    return this.http.get(`${Api.BASE_URL}/theories/trending?limit=${limit}`);
  }

  getLastChapterTheories(limit: number = 10): Observable<any>{
    return this.http.get(`${Api.BASE_URL}/theories/last-chapter?limit=${limit}`);
  }

  changePassword(currentPassword: string, newPassword: string, confirmPassword: string): Observable<any> {
    return this.http.put(`${Api.BASE_URL}/users/change-password`, {
      currentPassword,
      newPassword,
      confirmPassword
    }, {
      headers: this.getHeader(),
    });
  }

  getTheoryById(id: string): Observable<any> {
    return this.http.get(`${Api.BASE_URL}/theories/${id}`);
  }

  getTheoryVersions(id: string): Observable<any> {
    return this.http.get(`${Api.BASE_URL}/theories/${id}/versions`);
  }

  getTheoriesByUserId(userId: string): Observable<any> {
    return this.http.get(`${Api.BASE_URL}/theories/user/${userId}`);
  }

  updateTheory(formData: any): Observable<any> {
    const id = (formData && formData.get) ? formData.get('id') : (formData?.id || '');
    return this.http.put(
      `${Api.BASE_URL}/theories/update/${id}`,
      formData,
      {
        headers: this.getHeader(),
      }
    );
  }

  deleteTheory(id: string): Observable<any> {
    return this.http.delete(`${Api.BASE_URL}/theories/delete/${id}`, {
      headers: this.getHeader(),
    });
  }


  /** COMMENTS API */
  postComment(body: any): Observable<any> {
    return this.http.post(`${Api.BASE_URL}/comments/post`, body, {
      headers: this.getHeader(),
    });
  }

  getAllTheoryComments(id: string): Observable<any> {
    return this.http.get(`${Api.BASE_URL}/comments/theory/${id}`);
  }

  getCommentById(id: string): Observable<any> {
    return this.http.get(`${Api.BASE_URL}/comments/${id}`);
  }

  updateComment(id: string, body: any): Observable<any> {
    return this.http.put(
      `${Api.BASE_URL}/comments/update/${id}`,
      body,
      {
        headers: this.getHeader(),
      }
    );
  }

  deleteComment(id: string): Observable<any> {
    return this.http.delete(`${Api.BASE_URL}/comments/delete/${id}`, {
      headers: this.getHeader(),
    });
  }


  /**VOTES ENDPOINTS */ 
  upVote(body: any): Observable<any> {
    return this.http.post(`${Api.BASE_URL}/votes/up`, body, {
      headers: this.getHeader(),
    });
  }

  downVote(body: any): Observable<any> {
    return this.http.post(`${Api.BASE_URL}/votes/down`, body, {
      headers: this.getHeader(),
    });
  }

  unVote(body: any): Observable<any> {
    return this.http.post(`${Api.BASE_URL}/votes/unvote`, body, {
      headers: this.getHeader(),
    });
  }

  getAllTheoryUpVotes(id: string): Observable<any> {
    return this.http.get(`${Api.BASE_URL}/votes/up/${id}`); // Theory ID
  }

  getAllTheoryDownVotes(id: string): Observable<any> {
    return this.http.get(`${Api.BASE_URL}/votes/down/${id}`); // Theory ID
  }

  getAllUserVotes(id: string): Observable<any> {
    return this.http.get(`${Api.BASE_URL}/votes/user/${id}`); // User ID
  }

  getUserVote(userId: string, theoryId: string): Observable<any> {
    return this.http.get(`${Api.BASE_URL}/votes/user/${userId}/theory/${theoryId}`); // User ID, Theory ID
  }

  /**Daily update */ 
  getDailySeries(theoryId: string | number, days = 30){
    return this.http.get<Array<{day:string; upRatio:number; upCount:number; downCount:number}>>(
      `${Api.BASE_URL}/theories/${theoryId}/daily-series?days=${days}`
    );
  }


  // Upload Images
  uploadImage(formData: any): Observable<any>{
    return this.http.post(
      `${Api.BASE_URL}/uploads/image`,
      formData,
      {
        headers: this.getHeader(),
      }
    );
  }
  
  /**BOOKMARKS ENDPOINTS */
  addBookmark(theoryId: number): Observable<any> {
    return this.http.post(`${Api.BASE_URL}/bookmarks/add`, { theoryId }, {
      headers: this.getHeader(),
    });
  }

  removeBookmark(theoryId: number): Observable<any> {
    return this.http.delete(`${Api.BASE_URL}/bookmarks/remove/${theoryId}`, {
      headers: this.getHeader(),
    });
  }

  getMyBookmarks(): Observable<any> {
    return this.http.get(`${Api.BASE_URL}/bookmarks/my-bookmarks`, {
      headers: this.getHeader(),
    });
  }

  isBookmarked(theoryId: number): Observable<any> {
    return this.http.get(`${Api.BASE_URL}/bookmarks/is-bookmarked/${theoryId}`, {
      headers: this.getHeader(),
    });
  }

  //AUTHENTICATION CHECKER
  logout():void{
    this.clearAuth()
  }
  isAuthenticated():boolean{
    const token = this.getFromStorageAndDecrypt("token");
    return !!token;
  }
  isAdmin():boolean{
    const role = this.getFromStorageAndDecrypt("role");
    return role == "ADMIN";
  }
  isModerator():boolean{
    const role = this.getFromStorageAndDecrypt("role");
    return role == "MODERATOR" || role == "ADMIN";
  }
}
