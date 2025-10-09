import { Component, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit{

  message: string = "Loading..."
  protected readonly title = signal('frontend');

  ngOnInit() {
    fetch("http://localhost:8080/api/hello")
      .then(res => res.text())
      .then(data => this.message = data)
      .catch(() => this.message = "Backend not reachable");
  }
}
