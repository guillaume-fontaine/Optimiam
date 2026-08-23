package fr.trollgun.optimiam.planning.application;

import fr.trollgun.optimiam.common.event.DomainEvent;
import fr.trollgun.optimiam.common.event.DomainEventPublisher;
import fr.trollgun.optimiam.planning.api.dto.CreateMealPlanRequest;
import fr.trollgun.optimiam.planning.api.dto.MealPlanResponse;
import fr.trollgun.optimiam.planning.domain.MealPlan;
import fr.trollgun.optimiam.planning.domain.MealPlanRepository;
import fr.trollgun.optimiam.planning.domain.MealPlanStatus;
import fr.trollgun.optimiam.planning.domain.MealType;
import fr.trollgun.optimiam.recipe.application.RecipeService;
import fr.trollgun.optimiam.recipe.domain.Recipe;
import fr.trollgun.optimiam.stock.application.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealPlanServiceTest {

    @Mock
    private MealPlanRepository mealPlanRepository;

    @Mock
    private RecipeService recipeService;

    @Mock
    private StockService stockService;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private MealPlanService mealPlanService;

    private Recipe recipe;
    private UUID recipeId;
    private UUID mealPlanId;

    @BeforeEach
    void setUp() {
        recipeId = UUID.randomUUID();
        mealPlanId = UUID.randomUUID();

        recipe = Recipe.builder()
                .id(recipeId)
                .name("Ratatouille provençale")
                .servings(4)
                .build();
    }

    @Test
    @DisplayName("Doit créer un repas planifié")
    void shouldCreateMealPlan() {
        CreateMealPlanRequest request = CreateMealPlanRequest.builder()
                .date(LocalDate.now())
                .mealType(MealType.DINNER)
                .recipeId(recipeId)
                .servings(4)
                .notes("Repas du soir")
                .build();

        when(recipeService.getRecipeEntity(recipeId)).thenReturn(recipe);
        when(mealPlanRepository.save(any(MealPlan.class))).thenAnswer(invocation -> {
            MealPlan m = invocation.getArgument(0);
            m.setId(mealPlanId);
            return m;
        });

        MealPlanResponse response = mealPlanService.createMealPlan(request);

        assertThat(response).isNotNull();
        assertThat(response.getMealType()).isEqualTo(MealType.DINNER);
        assertThat(response.getStatus()).isEqualTo(MealPlanStatus.PLANNED);
        verify(mealPlanRepository).save(any(MealPlan.class));
        verify(domainEventPublisher).publish(any(DomainEvent.class));
    }

    @Test
    @DisplayName("Doit valider un repas comme cuisiné")
    void shouldMarkMealAsCooked() {
        MealPlan mealPlan = MealPlan.builder()
                .id(mealPlanId)
                .date(LocalDate.now())
                .mealType(MealType.LUNCH)
                .recipe(recipe)
                .servings(4)
                .status(MealPlanStatus.PLANNED)
                .build();

        when(mealPlanRepository.findById(mealPlanId)).thenReturn(Optional.of(mealPlan));
        when(mealPlanRepository.save(any(MealPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MealPlanResponse response = mealPlanService.markAsCooked(mealPlanId, false);

        assertThat(response.getStatus()).isEqualTo(MealPlanStatus.COOKED);
        verify(domainEventPublisher).publish(any(DomainEvent.class));
    }
}
