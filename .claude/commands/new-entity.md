Create a new Room database entity for: $ARGUMENTS

Follow the project's existing patterns:
1. Create `data/local/entity/{Name}Entity.kt` with `@Entity` annotation and a proper table name
2. Create `data/local/dao/{Name}Dao.kt` with `@Dao` annotation and suspend CRUD functions
3. Register the entity in `data/local/AppDatabase.kt`:
   - Add to `@Database(entities = [...])`
   - Add abstract DAO getter function
   - Create a new migration (increment from current version 4 to 5) with the CREATE TABLE SQL
   - Add the migration to `.addMigrations()` call

Reference existing entities (e.g., `FocusModeEntity.kt`, `StreakEntity.kt`) and DAOs for the pattern.