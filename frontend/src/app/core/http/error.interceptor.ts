import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from '../services/notification.service';
import { ErrorResponse } from '../models/error-response.model';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const notificationService = inject(NotificationService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'Une erreur inattendue est survenue.';

      if (error.error && typeof error.error === 'object') {
        const errorResponse = error.error as ErrorResponse;
        if (errorResponse.message) {
          errorMessage = errorResponse.message;
        }
      } else if (error.status === 0) {
        errorMessage = 'Impossible de contacter le serveur backend.';
      } else if (error.status === 404) {
        errorMessage = 'Ressource introuvable.';
      } else if (error.status === 409) {
        errorMessage = 'Conflit de données détecté.';
      }

      notificationService.error(errorMessage);
      return throwError(() => error);
    })
  );
};
