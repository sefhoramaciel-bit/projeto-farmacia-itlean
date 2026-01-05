import { Routes } from '@angular/router';
import { LayoutComponent } from './components/layout/layout.component';
import { AuthGuard } from './services/auth.guard';
import { AdminGuard } from './services/role.guard';

export const APP_ROUTES: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then(c => c.LoginComponent),
  },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: '', redirectTo: 'inicio', pathMatch: 'full' },
      { 
        path: 'inicio', 
        loadComponent: () => import('./pages/home/home.component').then(c => c.HomeComponent) 
      },
      { 
        path: 'medicamentos', 
        loadComponent: () => import('./pages/medicines/medicines.component').then(c => c.MedicinesComponent) 
      },
      { 
        path: 'categorias', 
        loadComponent: () => import('./pages/categories/categories.component').then(c => c.CategoriesComponent) 
      },
      { 
        path: 'categorias/:id/medicamentos', 
        loadComponent: () => import('./pages/categories/category-medicines.component').then(c => c.CategoryMedicinesComponent)
      },
      { 
        path: 'clientes', 
        loadComponent: () => import('./pages/customers/customers.component').then(c => c.CustomersComponent) 
      },
      { 
        path: 'estoque', 
        loadComponent: () => import('./pages/stock/stock.component').then(c => c.StockComponent) 
      },
      { 
        path: 'vendas', 
        loadComponent: () => import('./pages/sales/sales.component').then(c => c.SalesComponent) 
      },
      { 
        path: 'logs', 
        loadComponent: () => import('./pages/logs/logs.component').then(c => c.LogsComponent),
        canActivate: [AdminGuard]
      },
      { 
        path: 'usuarios', 
        loadComponent: () => import('./pages/users/users.component').then(c => c.UsersComponent),
        canActivate: [AdminGuard]
      },
    ]
  },
  { path: '**', redirectTo: '/login' }
];