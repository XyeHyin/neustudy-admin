<template>
  <n-icon v-bind="props"><component :is="icon" /></n-icon>
</template>

<script lang="ts" setup>
import {
  AddOutline,
  AlertCircleOutline,
  BookOutline,
  BulbOutline,
  ConstructOutline,
  CubeOutline,
  DocumentTextOutline,
  FlaskOutline,
  FolderOpenOutline,
  GridOutline,
  HelpCircleOutline,
  KeyOutline,
  ListOutline,
  LogoGithub,
  NotificationsOutline,
  PeopleOutline,
  PersonCircleOutline,
  ReceiptOutline,
  RefreshOutline,
  SchoolOutline,
  SpeedometerOutline,
  MoonOutline
} from '@vicons/ionicons5'
import {
  AddRound as Add,
  ArrowForwardIosOutlined as ArrowForward,
  BarChartOutlined as BarChart,
  CategoryOutlined as Category,
  PeopleAltOutlined as People,
  PlayArrowOutlined as PlayArrow,
  QuizOutlined as Quiz,
  SchoolOutlined as School,
  SettingsOutlined as Settings,
  TrendingDownOutlined as TrendingDown,
  TrendingUpOutlined as TrendingUp,
  WbSunnyOutlined as WeatherSunny,
  AutoAwesomeOutlined as AutoAwesome,
  CheckCircleOutlined as CheckCircle,
  CloseOutlined as Close,
  SaveOutlined as Save,
  SearchOutlined as Search,
  StarOutlined as Star,
  DownloadOutlined as Download,
  DeleteForeverOutlined as DeleteForever,
  DocumentScannerOutlined as DocumentText
} from '@vicons/material'
import { NIcon } from 'naive-ui'
import { computed } from 'vue'

const icons = {
  refresh: RefreshOutline,
  dashboard: SpeedometerOutline,
  product: CubeOutline,
  order: ReceiptOutline,
  users: PeopleOutline,
  role: PersonCircleOutline,
  category: FolderOpenOutline,
  permission: KeyOutline,
  flask: FlaskOutline,
  add: AddOutline,
  help: HelpCircleOutline,
  notifications: NotificationsOutline,
  github: LogoGithub,
  fallback: AlertCircleOutline,
  bulb: BulbOutline,
  list: ListOutline,
  grid: GridOutline,
  knowledgePoint: BookOutline,
  school: SchoolOutline,
  usersOutline: PeopleOutline,
  documentText: DocumentTextOutline,
  construct: ConstructOutline,
  moon: MoonOutline,
  materialAdd: Add,
  materialArrowForward: ArrowForward,
  materialBarChart: BarChart,
  materialCategory: Category,
  materialPeople: People,
  materialPlayArrow: PlayArrow,
  materialQuiz: Quiz,
  materialSchool: School,
  materialSettings: Settings,
  materialTrendingDown: TrendingDown,
  materialTrendingUp: TrendingUp,
  materialWeatherSunny: WeatherSunny,
  materialAutoAwesome: AutoAwesome,
  materialCheckCircle: CheckCircle,
  materialClose: Close,
  materialSearch: Search,
  materialStar: Star,
  materialSave: Save,
  materialDownload: Download,
  materialDeleteForever: DeleteForever,
  materialDocument: DocumentText
}

type IconTypes = keyof typeof icons

const props = defineProps({
  ...NIcon.props,
  type: {
    type: String,
    default: 'fallback'
  }
})

const icon = computed(() => icons[props.type as IconTypes] ?? icons.fallback)
</script>
